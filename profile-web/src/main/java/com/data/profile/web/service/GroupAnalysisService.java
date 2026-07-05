package com.data.profile.web.service;

import com.data.profile.common.enums.*;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.web.converter.GroupAnalysisConverter;
import com.data.profile.web.dao.GroupAnalysisMapper;
import com.data.profile.web.dao.LabelMapper;
import com.data.profile.web.dto.GroupAnalysisRequest;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.model.*;
import com.data.profile.web.security.RequestContext;
import com.data.profile.web.vo.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.data.profile.common.domain.Constant.ENGINE_DATASET_TABLE_PREFIX;
import static com.data.profile.common.domain.Constant.ENGINE_GROUP_TABLE_PREFIX;

/**
 * 功能：群组分析服务
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class GroupAnalysisService {
    private static final Gson gson = new GsonBuilder().create();

    @Autowired
    private GroupService groupService;
    @Resource
    private LabelMapper labelMapper;
    @Resource
    private GroupAnalysisMapper groupAnalysisMapper;
    @Autowired
    private DatasetFieldService datasetFieldService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private LabelCategoryService labelCategoryService;
    @Autowired
    private AnalysisEngineService analysisEngineService;

    /**
     * 获取可分析的群组列表（groupCount > 0）
     */
    public List<GroupVO> getAnalyzableGroups(Group query) {
        List<GroupVO> allGroups = groupService.getList(query);
        List<GroupVO> analyzable = allGroups.stream()
                .filter(g -> g.getGroupCount() != null && g.getGroupCount() > 0)
                .collect(Collectors.toList());
        log.info("获取可分析群组: 共 {} 个", analyzable.size());
        return analyzable;
    }

    // ========== 群组分析 CRUD ==========

    /**
     * 获取群组分析列表
     */
    public List<GroupAnalysisVO> getAnalysisList(GroupAnalysis query) {
        List<GroupAnalysis> list = groupAnalysisMapper.selectByParams(query);
        List<GroupAnalysisVO> vos = list.stream().map(this::toAnalysisVO).collect(Collectors.toList());
        log.info("获取群组分析列表: {} 个", vos.size());
        return vos;
    }

    /**
     * 获取群组分析详情
     */
    public Optional<GroupAnalysisVO> getAnalysisDetail(String analysisId) {
        GroupAnalysis analysis = groupAnalysisMapper.selectByAnalysisId(analysisId);
        if (analysis == null) {
            return Optional.empty();
        }
        return Optional.of(toAnalysisVO(analysis));
    }

    /**
     * 保存群组分析（新增/修改）
     */
    public int saveAnalysis(GroupAnalysis analysis) {
        // 将 List<String> 转为 JSON 字符串存储
        if (analysis.getCompareGroupIdList() != null) {
            analysis.setCompareGroupIds(gson.toJson(analysis.getCompareGroupIdList()));
        }
        if (analysis.getLabelIdList() != null) {
            analysis.setLabelIds(gson.toJson(analysis.getLabelIdList()));
        }
        if (analysis.getAnalysisId() == null || analysis.getAnalysisId().isEmpty()) {
            // 新增
            String analysisId = IDGenerator.getInstance().generate(ModelType.ANALYSIS);
            analysis.setAnalysisId(analysisId);
            analysis.setStatus(Status.ENABLE.getCode());
            analysis.setSourceType(2);
            analysis.setOwner(RequestContext.currentUserId());
            analysis.setCreator(RequestContext.currentUserId());
            analysis.setModifier(RequestContext.currentUserId());
            log.info("新增群组分析: {}", gson.toJson(analysis));
            return groupAnalysisMapper.insertSelective(analysis);
        } else {
            // 修改
            analysis.setModifier(RequestContext.currentUserId());
            log.info("修改群组分析: {}", gson.toJson(analysis));
            return groupAnalysisMapper.updateByAnalysisIdSelective(analysis);
        }
    }

    /**
     * 删除群组分析
     */
    public int deleteAnalysis(String analysisId) {
        GroupAnalysis analysis = groupAnalysisMapper.selectByAnalysisId(analysisId);
        if (analysis == null) {
            throw new RuntimeException("群组分析不存在");
        }
        log.info("删除群组分析: {}", analysisId);
        return groupAnalysisMapper.deleteByAnalysisId(analysisId);
    }

    /**
     * 将 GroupAnalysis DO 转换为 VO（填充群组关联信息）
     */
    private GroupAnalysisVO toAnalysisVO(GroupAnalysis analysis) {
        GroupAnalysisVO vo = new GroupAnalysisVO();
        BeanUtils.copyProperties(analysis, vo);
        // 解析 JSON 数组
        vo.setCompareGroupIds(parseJsonArray(analysis.getCompareGroupIds()));
        vo.setLabelIds(parseJsonArray(analysis.getLabelIds()));
        // 填充群组信息
        Optional<GroupVO> groupOpt = groupService.getDetail(analysis.getGroupId());
        if (groupOpt.isPresent()) {
            GroupVO group = groupOpt.get();
            vo.setGroupName(group.getGroupName());
            vo.setGroupCount(group.getGroupCount());
            vo.setGroupType(group.getGroupType());
            vo.setGroupStatus(group.getGroupStatus());
            vo.setEntityIdentifierName(group.getEntityIdentifierName());
            vo.setEntityName(group.getEntityName());
        }
        return vo;
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyList();
        try {
            return gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
        } catch (Exception e) {
            log.warn("解析 JSON 数组失败: {}", json);
            return Collections.emptyList();
        }
    }

    /**
     * 获取可分析标签列表
     * <p>路径: entityIdentifierId -> Dataset(entityId) -> DatasetField(relatedId=labelId) -> Label</p>
     * <p>过滤: labelDistType==1(枚举型) + labelStatus==1(启用)</p>
     */
    public List<AnalysisLabelVO> getAvailableLabels(String entityIdentifierId) {
        // 1. 查询该实体标识下所有启用且枚举型标签
        Label query = new Label();
        query.setEntityIdentifierId(entityIdentifierId);
        query.setLabelDistType(LabelDistType.ENUM.getCode());
        // TODO 只要绑定标签
        query.setLabelStatus(LabelStatus.CREATED.getCode());

        List<Label> labels = labelMapper.selectByParams(query);
        log.info("实体 [{}] 下共 {} 个标签", entityIdentifierId, labels.size());

        // 3. 构建类目缓存
        Map<String, String> categoryNameMap = new HashMap<>();
        LabelCategory catQuery = new LabelCategory();
        List<LabelCategory> categories = labelCategoryService.getList(catQuery);
        for (LabelCategory cat : categories) {
            categoryNameMap.put(cat.getCategoryId(), cat.getCategoryName());
        }

        // 4. 构建数据集缓存
        Map<String, Dataset> datasetCache = new HashMap<>();

        // 5. 对每个标签查找 datasetField -> dataset
        // TODO 绑定标签需要更新状态
        List<AnalysisLabelVO> result = new ArrayList<>();
        for (Label label : labels) {
            DatasetField field = datasetFieldService.getDetailByRelatedId(label.getLabelId());
            if (field == null) {
                log.debug("标签 {} 未关联数据集字段，跳过", label.getLabelId());
                continue;
            }

            String datasetId = field.getDatasetId();
            Dataset dataset = datasetCache.computeIfAbsent(datasetId, id -> {
                Optional<Dataset> opt = datasetService.getDetailModel(id);
                return opt.orElse(null);
            });
            if (dataset == null) {
                log.debug("标签 {} 关联的数据集 {} 不存在，跳过", label.getLabelId(), datasetId);
                continue;
            }

            // 构建 VO
            AnalysisLabelVO vo = GroupAnalysisConverter.toAnalysisLabelVO(label);
            vo.setDatasetId(datasetId);
            vo.setDatasetName(dataset.getDatasetName());
            vo.setFieldName(field.getFieldName());
            vo.setLabelCategoryName(categoryNameMap.getOrDefault(label.getLabelCategoryId(), "未分类"));

            result.add(vo);
        }

        log.info("可分析标签: {} 个", result.size());
        return result;
    }

    /**
     * 获取标签分布数据
     * <p>按 datasetId 分组批量查询，减少 SQL 执行次数</p>
     */
    public List<LabelDistributionVO> getLabelDistribution(GroupAnalysisRequest request) {
        String groupId = request.getGroupId();
        List<String> labelIds = request.getLabelIds();
        List<String> compareGroupIds = request.getCompareGroupIds();

        if (labelIds == null || labelIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 解析每个标签的元数据: labelId -> {datasetId, fieldName, entityField, datasetName}
        Map<String, LabelMeta> labelMetaMap = new LinkedHashMap<>();
        for (String labelId : labelIds) {
            LabelMeta meta = resolveLabelMeta(labelId);
            if (meta != null) {
                labelMetaMap.put(labelId, meta);
            }
        }

        // 2. 按 datasetId 分组
        Map<String, List<Map.Entry<String, LabelMeta>>> byDataset = labelMetaMap.entrySet().stream()
                .collect(Collectors.groupingBy(e -> e.getValue().datasetId));

        // 3. 对每个 dataset 批量查询分布
        Map<String, Map<String, long[]>> distributionResults = new LinkedHashMap<>();
        for (Map.Entry<String, List<Map.Entry<String, LabelMeta>>> entry : byDataset.entrySet()) {
            String datasetId = entry.getKey();
            List<Map.Entry<String, LabelMeta>> labelsInDataset = entry.getValue();
            String entityField = labelsInDataset.get(0).getValue().entityField;
            String datasetTable = ENGINE_DATASET_TABLE_PREFIX + datasetId;
            String groupTable = ENGINE_GROUP_TABLE_PREFIX + groupId;

            for (Map.Entry<String, LabelMeta> labelEntry : labelsInDataset) {
                String labelId = labelEntry.getKey();
                String fieldName = labelEntry.getValue().fieldName;

                try {
                    // 当前人群分布
                    String currentSql = String.format(
                            "SELECT d.%s AS label_value, COUNT(*) AS cnt " +
                                    "FROM %s d INNER JOIN %s g ON g.entity_id = d.%s " +
                                    "WHERE d.%s IS NOT NULL AND d.%s != '' " +
                                    "GROUP BY d.%s",
                            fieldName, datasetTable, groupTable, entityField,
                            fieldName, fieldName, fieldName);
                    log.info("查询当前人群分布 SQL: {}", currentSql);
                    List<Map<String, Object>> currentRows = analysisEngineService.executeQueryList(currentSql);

                    // 全体人群分布
                    String allSql = String.format(
                            "SELECT %s AS label_value, COUNT(*) AS cnt " +
                                    "FROM %s " +
                                    "WHERE %s IS NOT NULL AND %s != '' " +
                                    "GROUP BY %s",
                            fieldName, datasetTable, fieldName, fieldName, fieldName);
                    log.info("查询全体人群分布 SQL: {}", allSql);
                    List<Map<String, Object>> allRows = analysisEngineService.executeQueryList(allSql);

                    // 对比群组分布（如有）
                    List<Map<String, Object>> compareRows = null;
                    if (compareGroupIds != null && !compareGroupIds.isEmpty()) {
                        String compareGroupId = compareGroupIds.get(0);
                        String compareGroupTable = ENGINE_GROUP_TABLE_PREFIX + compareGroupId;
                        String compareSql = String.format(
                                "SELECT d.%s AS label_value, COUNT(*) AS cnt " +
                                        "FROM %s d INNER JOIN %s g ON g.entity_id = d.%s " +
                                        "WHERE d.%s IS NOT NULL AND d.%s != '' " +
                                        "GROUP BY d.%s",
                                fieldName, datasetTable, compareGroupTable, entityField,
                                fieldName, fieldName, fieldName);
                        log.info("查询对比群组分布 SQL: {}", compareSql);
                        compareRows = analysisEngineService.executeQueryList(compareSql);
                    }

                    // 合并结果
                    Map<String, long[]> merged = mergeDistribution(currentRows, allRows, compareRows);
                    distributionResults.put(labelId, merged);

                } catch (Exception e) {
                    log.error("查询标签 {} 分布失败: {}", labelId, e.getMessage(), e);
                    distributionResults.put(labelId, Collections.emptyMap());
                }
            }
        }

        // 4. 构建响应 VO
        List<LabelDistributionVO> result = new ArrayList<>();
        for (String labelId : labelIds) {
            LabelMeta meta = labelMetaMap.get(labelId);
            if (meta == null) continue;

            LabelDistributionVO dist = new LabelDistributionVO();
            dist.setLabelId(labelId);
            dist.setLabelName(meta.labelName);
            dist.setDatasetName(meta.datasetName);
            dist.setUpdateType(meta.updateType == 2 ? "周期更新" : "手动更新");

            Map<String, long[]> values = distributionResults.getOrDefault(labelId, Collections.emptyMap());
            long currentTotal = values.values().stream().mapToLong(v -> v[0]).sum();
            long allTotal = values.values().stream().mapToLong(v -> v[1]).sum();
            long compareTotal = values.values().stream().mapToLong(v -> v[2]).sum();

            List<DistributionItemVO> items = new ArrayList<>();
            for (Map.Entry<String, long[]> valEntry : values.entrySet()) {
                DistributionItemVO item = new DistributionItemVO();
                item.setValue(valEntry.getKey());
                long[] counts = valEntry.getValue();
                item.setCurrentCount(counts[0]);
                item.setCurrentRate(currentTotal > 0 ? (double) counts[0] / currentTotal * 100 : 0.0);
                item.setAllCount(counts[1]);
                item.setAllRate(allTotal > 0 ? (double) counts[1] / allTotal * 100 : 0.0);
                if (compareGroupIds != null && !compareGroupIds.isEmpty()) {
                    item.setCompareCount(counts[2]);
                    item.setCompareRate(compareTotal > 0 ? (double) counts[2] / compareTotal * 100 : 0.0);
                }
                items.add(item);
            }

            // 按 currentCount 降序排序
            items.sort((a, b) -> Long.compare(b.getCurrentCount(), a.getCurrentCount()));
            dist.setValues(items);
            result.add(dist);
        }

        return result;
    }

    /**
     * 解析标签元数据: labelId -> DatasetField -> Dataset
     */
    private LabelMeta resolveLabelMeta(String labelId) {
        Label label = labelMapper.selectByLabelId(labelId);
        if (label == null) {
            log.warn("标签 {} 不存在", labelId);
            return null;
        }

        DatasetField field = datasetFieldService.getDetailByRelatedId(labelId);
        if (field == null) {
            log.warn("标签 {} 未关联数据集字段", labelId);
            return null;
        }

        Optional<Dataset> datasetOpt = datasetService.getDetailModel(field.getDatasetId());
        if (!datasetOpt.isPresent()) {
            log.warn("标签 {} 关联的数据集不存在", labelId);
            return null;
        }
        Dataset dataset = datasetOpt.get();

        LabelMeta meta = new LabelMeta();
        meta.labelId = labelId;
        meta.labelName = label.getLabelName();
        meta.datasetId = dataset.getDatasetId();
        meta.datasetName = dataset.getDatasetName();
        meta.fieldName = field.getFieldName();
        meta.entityField = dataset.getEntityField();
        meta.updateType = label.getSourceType() != null && label.getSourceType() == 2 ? 2 : 1;
        return meta;
    }

    /**
     * 合并当前人群、全体人群、对比群组的分布结果
     */
    private Map<String, long[]> mergeDistribution(
            List<Map<String, Object>> currentRows,
            List<Map<String, Object>> allRows,
            List<Map<String, Object>> compareRows) {
        Map<String, long[]> merged = new LinkedHashMap<>();

        // 当前人群
        for (Map<String, Object> row : currentRows) {
            String value = String.valueOf(row.get("label_value"));
            long count = ((Number) row.get("cnt")).longValue();
            merged.computeIfAbsent(value, k -> new long[3]);
            merged.get(value)[0] = count;
        }

        // 全体人群
        for (Map<String, Object> row : allRows) {
            String value = String.valueOf(row.get("label_value"));
            long count = ((Number) row.get("cnt")).longValue();
            merged.computeIfAbsent(value, k -> new long[3]);
            merged.get(value)[1] = count;
        }

        // 对比群组
        if (compareRows != null) {
            for (Map<String, Object> row : compareRows) {
                String value = String.valueOf(row.get("label_value"));
                long count = ((Number) row.get("cnt")).longValue();
                merged.computeIfAbsent(value, k -> new long[3]);
                merged.get(value)[2] = count;
            }
        }

        return merged;
    }

    /**
     * 标签元数据内部类
     */
    private static class LabelMeta {
        String labelId;
        String labelName;
        String datasetId;
        String datasetName;
        String fieldName;
        String entityField;
        int updateType;
    }
}
