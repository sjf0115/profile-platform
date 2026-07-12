package com.data.profile.web.service;

import com.data.profile.common.exception.ProfileException;
import com.data.profile.web.dao.*;
import com.data.profile.web.dto.ApplicationDTO;
import com.data.profile.web.dto.ExportDTO;
import com.data.profile.web.dto.GroupDTO;
import com.data.profile.web.enums.AssetType;
import com.data.profile.web.enums.RelationType;
import com.data.profile.web.model.*;
import com.data.profile.web.service.lineage.LineageExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 血缘服务
 */
@Slf4j
@Service
public class LineageService {

    @Autowired
    private LineageEdgeMapper lineageEdgeMapper;

    @Autowired
    private List<LineageExtractor> extractors;

    // resolveNodeName / rebuildAll 所需的 Mapper
    @Autowired
    private DatasetMapper datasetMapper;
    @Autowired
    private DataSourceMapper dataSourceMapper;
    @Autowired
    private LabelMapper labelMapper;
    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private GroupAnalysisMapper groupAnalysisMapper;
    @Autowired
    private ExportMapper exportMapper;
    @Autowired
    private ApplicationMapper applicationMapper;

    // TODO
    // listNodes 使用的各模块 Service（@Lazy 避免循环依赖）
    @Autowired @Lazy private DataSourceService dataSourceService;
    @Autowired @Lazy private DatasetService datasetService;
    @Autowired @Lazy private LabelService labelService;
    @Autowired @Lazy private EventService eventService;
    @Autowired @Lazy private GroupService groupService;
    @Autowired @Lazy private GroupAnalysisService groupAnalysisService;
    @Autowired @Lazy private ExportService exportService;
    @Autowired @Lazy private ApplicationService applicationService;

    private Map<String, LineageExtractor> extractorMap;

    @PostConstruct
    public void init() {
        extractorMap = extractors.stream()
                .collect(Collectors.toMap(LineageExtractor::assetType, Function.identity()));
        log.info("血缘抽取器注册完成: {}", extractorMap.keySet());
    }

    // ========== 写侧 ==========

    /**
     * 刷新指定资产的上游血缘边（幂等：先删后插）
     */
    public void refreshLineage(String assetType, String assetId) {
        LineageExtractor extractor = extractorMap.get(assetType);
        if (extractor == null) {
            log.warn("未找到资产类型 {} 对应的血缘抽取器", assetType);
            return;
        }
        List<LineageEdge> edges = extractor.extractUpstreams(assetId);
        lineageEdgeMapper.deleteByDownstream(assetType, assetId);
        if (!edges.isEmpty()) {
            lineageEdgeMapper.batchInsert(edges);
        }
    }

    /**
     * 移除指定资产的全部血缘边（出入边）
     */
    public void removeLineage(String assetType, String assetId) {
        lineageEdgeMapper.deleteByNode(assetType, assetId);
    }

    // ========== 一跳查询（删除保护） ==========

    /**
     * 查找依赖该资产的下游节点
     */
    public List<LineageEdge> findDependents(String assetType, String assetId) {
        return lineageEdgeMapper.selectByUpstream(assetType, assetId);
    }

    /**
     * 删除保护校验
     */
    public void checkDeletable(String assetType, String assetId) {
        List<LineageEdge> dependents = findDependents(assetType, assetId);
        if (dependents != null && !dependents.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (LineageEdge edge : dependents) {
                String typeName = AssetType.getNameByCode(edge.getDownstreamType());
                sb.append(typeName).append("(").append(edge.getDownstreamId()).append("), ");
            }
            sb.setLength(sb.length() - 2);
            throw new ProfileException("该资产被 " + dependents.size() + " 个下游引用，无法删除：" + sb);
        }
    }

    // ========== 多跳查询 ==========

    /**
     * BFS 遍历血缘图
     */
    public LineageGraphVO getGraph(String nodeType, String nodeId, String direction, int depth) {
        if (depth <= 0) depth = 3;
        if (depth > 10) depth = 10;

        Set<String> visited = new HashSet<>();
        List<LineageGraphVO.LineageEdgeVO> allEdges = new ArrayList<>();
        Set<String> nodeKeys = new LinkedHashSet<>(); // type:id

        String startKey = nodeType + ":" + nodeId;
        visited.add(startKey);
        nodeKeys.add(startKey);

        List<NodeRef> currentLevel = Collections.singletonList(new NodeRef(nodeType, nodeId));

        for (int d = 0; d < depth && !currentLevel.isEmpty(); d++) {
            List<NodeRef> nextLevel = new ArrayList<>();

            // 上游方向
            if ("upstream".equals(direction) || "both".equals(direction)) {
                List<LineageEdge> upEdges = lineageEdgeMapper.selectUpstreamEdgesBatch(currentLevel);
                for (LineageEdge edge : upEdges) {
                    allEdges.add(LineageGraphVO.LineageEdgeVO.builder()
                            .source(edge.getUpstreamType() + ":" + edge.getUpstreamId())
                            .target(edge.getDownstreamType() + ":" + edge.getDownstreamId())
                            .relation(edge.getRelationType())
                            .relationName(RelationType.getNameByCode(edge.getRelationType()))
                            .build());
                    String upKey = edge.getUpstreamType() + ":" + edge.getUpstreamId();
                    nodeKeys.add(upKey);
                    nodeKeys.add(edge.getDownstreamType() + ":" + edge.getDownstreamId());
                    if (!visited.contains(upKey)) {
                        visited.add(upKey);
                        nextLevel.add(new NodeRef(edge.getUpstreamType(), edge.getUpstreamId()));
                    }
                }
            }

            // 下游方向
            if ("downstream".equals(direction) || "both".equals(direction)) {
                List<LineageEdge> downEdges = lineageEdgeMapper.selectDownstreamEdgesBatch(currentLevel);
                for (LineageEdge edge : downEdges) {
                    allEdges.add(LineageGraphVO.LineageEdgeVO.builder()
                            .source(edge.getUpstreamType() + ":" + edge.getUpstreamId())
                            .target(edge.getDownstreamType() + ":" + edge.getDownstreamId())
                            .relation(edge.getRelationType())
                            .relationName(RelationType.getNameByCode(edge.getRelationType()))
                            .build());
                    String downKey = edge.getDownstreamType() + ":" + edge.getDownstreamId();
                    nodeKeys.add(downKey);
                    nodeKeys.add(edge.getUpstreamType() + ":" + edge.getUpstreamId());
                    if (!visited.contains(downKey)) {
                        visited.add(downKey);
                        nextLevel.add(new NodeRef(edge.getDownstreamType(), edge.getDownstreamId()));
                    }
                }
            }

            currentLevel = nextLevel;
        }

        // 解析节点名称
        List<LineageGraphVO.LineageNodeVO> nodes = new ArrayList<>();
        for (String key : nodeKeys) {
            String[] parts = key.split(":", 2);
            String name = resolveNodeName(parts[0], parts[1]);
            nodes.add(LineageGraphVO.LineageNodeVO.builder()
                    .id(parts[1]).type(parts[0]).name(name).build());
        }

        return LineageGraphVO.builder().nodes(nodes).edges(allEdges).build();
    }

    /**
     * 影响分析（下游 BFS）
     */
    public List<ImpactVO> getImpact(String nodeType, String nodeId) {
        Set<String> visited = new HashSet<>();
        List<ImpactVO> result = new ArrayList<>();
        String startKey = nodeType + ":" + nodeId;
        visited.add(startKey);

        List<NodeRef> currentLevel = Collections.singletonList(new NodeRef(nodeType, nodeId));
        int depth = 0;

        while (!currentLevel.isEmpty() && depth < 10) {
            depth++;
            List<NodeRef> nextLevel = new ArrayList<>();
            List<LineageEdge> downEdges = lineageEdgeMapper.selectDownstreamEdgesBatch(currentLevel);
            for (LineageEdge edge : downEdges) {
                String downKey = edge.getDownstreamType() + ":" + edge.getDownstreamId();
                if (!visited.contains(downKey)) {
                    visited.add(downKey);
                    String name = resolveNodeName(edge.getDownstreamType(), edge.getDownstreamId());
                    result.add(ImpactVO.builder()
                            .nodeType(edge.getDownstreamType())
                            .nodeId(edge.getDownstreamId())
                            .nodeName(name)
                            .depth(depth)
                            .build());
                    nextLevel.add(new NodeRef(edge.getDownstreamType(), edge.getDownstreamId()));
                }
            }
            currentLevel = nextLevel;
        }
        return result;
    }

    /**
     * 解析节点名称
     */
    private String resolveNodeName(String type, String id) {
        try {
            AssetType assetType = AssetType.of(type);
            if (assetType == null) return id;
            switch (assetType) {
                case DATASOURCE:
                    DataSource ds = dataSourceMapper.selectByDatasourceId(id);
                    return ds != null ? ds.getDatasourceName() : id;
                case DATASET:
                    Dataset dataset = datasetMapper.selectByDatasetId(id);
                    return dataset != null ? dataset.getDatasetName() : id;
                case LABEL:
                    Label label = labelMapper.selectByLabelId(id);
                    return label != null ? label.getLabelName() : id;
                case EVENT:
                    Event event = eventMapper.selectByEventId(id);
                    return event != null ? event.getEventName() : id;
                case GROUP:
                    Group group = groupMapper.selectByGroupId(id);
                    return group != null ? group.getGroupName() : id;
                case ANALYSIS:
                    GroupAnalysis analysis = groupAnalysisMapper.selectByAnalysisId(id);
                    return analysis != null ? analysis.getAnalysisName() : id;
                case EXPORT:
                    Export export = exportMapper.selectByExportId(id);
                    return export != null ? export.getExportName() : id;
                case APPLICATION:
                    Application app = applicationMapper.selectByAppKey(id);
                    return app != null ? app.getAppName() : id;
                default:
                    return id;
            }
        } catch (Exception e) {
            log.warn("解析节点名称失败: type={}, id={}", type, id);
            return id;
        }
    }

    // ========== 节点列表（下拉框） ==========

    /**
     * 按资产类型查询所有节点（id + name）
     */
    public List<NodeRef> listNodes(String nodeType) {
        List<NodeRef> result = new ArrayList<>();
        try {
            AssetType assetType = AssetType.of(nodeType);
            if (assetType == null) return result;
            switch (assetType) {
                case DATASOURCE:
                    for (DataSource ds : dataSourceService.getList(new DataSource()))
                        result.add(new NodeRef(nodeType, ds.getDatasourceId(), ds.getDatasourceName()));
                    break;
                case DATASET:
                    for (Dataset ds : datasetService.getList(new Dataset()))
                        result.add(new NodeRef(nodeType, ds.getDatasetId(), ds.getDatasetName()));
                    break;
                case LABEL:
                    for (Label l : labelService.getList(new Label()))
                        result.add(new NodeRef(nodeType, l.getLabelId(), l.getLabelName()));
                    break;
                case EVENT:
                    for (Event e : eventService.getList(new Event()))
                        result.add(new NodeRef(nodeType, e.getEventId(), e.getEventName()));
                    break;
                case GROUP:
                    for (GroupDTO g : groupService.getList(new Group()))
                        result.add(new NodeRef(nodeType, g.getGroupId(), g.getGroupName()));
                    break;
                case ANALYSIS:
                    for (GroupAnalysis a : groupAnalysisService.getAnalysisList(new GroupAnalysis()))
                        result.add(new NodeRef(nodeType, a.getAnalysisId(), a.getAnalysisName()));
                    break;
                case EXPORT:
                    for (ExportDTO ex : exportService.getList(new Export()))
                        result.add(new NodeRef(nodeType, ex.getExportId(), ex.getExportName()));
                    break;
                case APPLICATION:
                    for (ApplicationDTO app : applicationService.getList(new Application()))
                        result.add(new NodeRef(nodeType, app.getAppKey(), app.getAppName()));
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("查询节点列表失败: nodeType={}", nodeType, e);
        }
        return result;
    }

    // ========== 全量重建 ==========

    /**
     * 全量重建血缘（首次初始化 + 灾难恢复）
     */
    public int rebuildAll() {
        lineageEdgeMapper.deleteAll();
        int count = 0;

        // 数据集
        List<Dataset> datasets = datasetMapper.selectByParams(new Dataset());
        for (Dataset ds : datasets) {
            refreshLineage(AssetType.DATASET.getCode(), ds.getDatasetId());
            count++;
        }

        // 标签
        List<Label> labels = labelMapper.selectByParams(new Label());
        for (Label label : labels) {
            refreshLineage(AssetType.LABEL.getCode(), label.getLabelId());
            count++;
        }

        // 事件
        List<Event> events = eventMapper.selectByParams(new Event());
        for (Event event : events) {
            refreshLineage(AssetType.EVENT.getCode(), event.getEventId());
            count++;
        }

        // 群组
        List<Group> groups = groupMapper.selectByParams(new Group());
        for (Group group : groups) {
            refreshLineage(AssetType.GROUP.getCode(), group.getGroupId());
            count++;
        }

        // 群组分析
        List<GroupAnalysis> analyses = groupAnalysisMapper.selectByParams(new GroupAnalysis());
        for (GroupAnalysis analysis : analyses) {
            refreshLineage(AssetType.ANALYSIS.getCode(), analysis.getAnalysisId());
            count++;
        }

        // 投递
        List<Export> exports = exportMapper.selectByParams(new Export());
        for (Export export : exports) {
            refreshLineage(AssetType.EXPORT.getCode(), export.getExportId());
            count++;
        }

        log.info("全量重建血缘完成，共处理 {} 个资产", count);
        return count;
    }
}
