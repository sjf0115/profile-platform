package com.data.profile.web.service;

import com.data.profile.common.enums.*;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.web.dao.GroupAnalysisMapper;
import com.data.profile.web.dao.LabelMapper;
import com.data.profile.web.dto.AnalysisLabelDTO;
import com.data.profile.web.dto.DistributionItemDTO;
import com.data.profile.web.dto.GroupAnalysisRequest;
import com.data.profile.web.dto.GroupDTO;
import com.data.profile.web.dto.LabelDistributionDTO;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.engine.SqlTemplateEngine;
import com.data.profile.web.model.*;
import com.data.profile.web.security.UserContextHolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private ResourceGrantService resourceGrantService;
    @Autowired
    private LabelMapper labelMapper;
    @Autowired
    private LabelService labelService;
    @Autowired
    private GroupAnalysisMapper groupAnalysisMapper;
    @Autowired
    private DatasetFieldService datasetFieldService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private LabelCategoryService labelCategoryService;
    @Autowired
    private AnalysisEngineService analysisEngineService;
    @Autowired
    private SqlTemplateEngine sqlTemplateEngine;

    /**
     * 获取可分析的群组列表（groupCount > 0）
     */
    public List<GroupDTO> getAnalyzableGroups(Group query) {
        List<GroupDTO> allGroups = groupService.getList(query);
        List<GroupDTO> analyzable = allGroups.stream()
                .filter(g -> g.getGroupCount() != null && g.getGroupCount() > 0)
                .collect(Collectors.toList());
        log.info("获取可分析群组: 共 {} 个", analyzable.size());
        return analyzable;
    }

    // ========== 群组分析 CRUD ==========

    /**
     * 获取群组分析列表（单表查询）
     */
    public List<GroupAnalysis> getAnalysisList(GroupAnalysis query) {
        List<GroupAnalysis> list = groupAnalysisMapper.selectByParams(query);
        log.info("获取群组分析列表: {} 个", list.size());
        return list;
    }

    /**
     * 获取群组分析详情（单表查询）
     */
    public Optional<GroupAnalysis> getAnalysisDetail(String analysisId) {
        GroupAnalysis analysis = groupAnalysisMapper.selectByAnalysisId(analysisId);
        if (analysis == null) {
            return Optional.empty();
        }
        return Optional.of(analysis);
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
            analysis.setOwner(UserContextHolder.currentUserId());
            analysis.setCreator(UserContextHolder.currentUserId());
            analysis.setModifier(UserContextHolder.currentUserId());
            log.info("新增群组分析: {}", gson.toJson(analysis));
            int result = groupAnalysisMapper.insertSelective(analysis);
            // 自动授权 MANAGE 给创建者
            resourceGrantService.grantOwner("22", analysisId, UserContextHolder.currentUserId());
            return result;
        } else {
            // 修改
            analysis.setModifier(UserContextHolder.currentUserId());
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
     * 获取可分析标签列表（三方 JOIN 聚合）
     * <p>路径: entityIdentifierId -> Dataset(entityId) -> DatasetField(relatedId=labelId) -> Label</p>
     * <p>过滤: labelDistType==1(枚举型) + labelStatus==1(启用)</p>
     */
    public List<AnalysisLabelDTO> getAvailableLabels(String entityIdentifierId) {
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
        List<AnalysisLabelDTO> result = new ArrayList<>();
        for (Label label : labels) {
            DatasetField field = datasetFieldService.getDetailByRelatedId(label.getLabelId());
            if (field == null) {
                log.debug("标签 {} 未关联数据集字段，跳过", label.getLabelId());
                continue;
            }

            String datasetId = field.getDatasetId();
            Dataset dataset = datasetCache.computeIfAbsent(datasetId, id -> {
                Optional<Dataset> opt = datasetService.getDetail(id);
                return opt.orElse(null);
            });
            if (dataset == null) {
                log.debug("标签 {} 关联的数据集 {} 不存在，跳过", label.getLabelId(), datasetId);
                continue;
            }

            // 构建 DTO
            AnalysisLabelDTO dto = new AnalysisLabelDTO();
            dto.setLabelId(label.getLabelId());
            dto.setLabelName(label.getLabelName());
            dto.setLabelCategoryId(label.getLabelCategoryId());
            dto.setLabelDataType(label.getLabelDataType());
            dto.setDatasetId(datasetId);
            dto.setDatasetName(dataset.getDatasetName());
            dto.setFieldName(field.getFieldName());
            dto.setLabelCategoryName(categoryNameMap.getOrDefault(label.getLabelCategoryId(), "未分类"));

            result.add(dto);
        }

        log.info("可分析标签: {} 个", result.size());
        return result;
    }

    /**
     * 获取单个标签的分布数据（计算型聚合）
     */
    public LabelDistributionDTO getLabelDistribution(GroupAnalysisRequest request) {
        String groupId = request.getGroupId();
        String labelId = request.getLabelId();
        List<String> compareGroupIds = request.getCompareGroupIds();

        // 1. 解析标签元数据
        LabelMeta meta = resolveLabelMeta(labelId);
        if (meta == null) {
            log.warn("标签 {} 元数据解析失败", labelId);
            return null;
        }

        String datasetTable = ENGINE_DATASET_TABLE_PREFIX + meta.datasetId;
        String groupTable = ENGINE_GROUP_TABLE_PREFIX + groupId;
        String fieldName = meta.fieldName;
        String entityField = meta.entityField;

        try {
            // 当前人群标签分布
            Map<String, Object> groupParams = new HashMap<>();
            groupParams.put("fieldName", fieldName);
            groupParams.put("datasetTable", datasetTable);
            groupParams.put("entityField", entityField);
            groupParams.put("groupTable", groupTable);
            String currentSql = sqlTemplateEngine.render("label_distribution.ftl", groupParams);
            log.info("查询当前人群分布 SQL: {}", currentSql);
            List<Map<String, Object>> currentRows = analysisEngineService.executeQueryList(currentSql);

            // 对比群组标签分布（如有）
            List<Map<String, Object>> compareRows = null;
            if (compareGroupIds != null && !compareGroupIds.isEmpty()) {
                String compareGroupId = compareGroupIds.get(0);
                String compareGroupTable = ENGINE_GROUP_TABLE_PREFIX + compareGroupId;
                groupParams.put("groupTable", compareGroupTable);
                String compareSql = sqlTemplateEngine.render("label_distribution.ftl", groupParams);
                log.info("查询对比群组分布 SQL: {}", compareSql);
                compareRows = analysisEngineService.executeQueryList(compareSql);
            }

            // 合并结果
            Map<String, long[]> merged = mergeDistribution(currentRows, compareRows);

            // 构建响应 DTO
            LabelDistributionDTO dist = new LabelDistributionDTO();
            dist.setLabelId(labelId);
            dist.setLabelName(meta.labelName);
            dist.setDatasetName(meta.datasetName);
            dist.setUpdateType(meta.updateType == 2 ? "周期更新" : "手动更新");

            long currentTotal = merged.values().stream().mapToLong(v -> v[0]).sum();
            long compareTotal = merged.values().stream().mapToLong(v -> v[1]).sum();

            List<DistributionItemDTO> items = new ArrayList<>();
            for (Map.Entry<String, long[]> valEntry : merged.entrySet()) {
                DistributionItemDTO item = new DistributionItemDTO();
                item.setValue(valEntry.getKey());
                long[] counts = valEntry.getValue();
                item.setCurrentCount(counts[0]);
                item.setCurrentRate(currentTotal > 0 ? (double) counts[0] / currentTotal * 100 : 0.0);
                if (compareGroupIds != null && !compareGroupIds.isEmpty()) {
                    item.setCompareCount(counts[1]);
                    item.setCompareRate(compareTotal > 0 ? (double) counts[1] / compareTotal * 100 : 0.0);
                }
                items.add(item);
            }

            // 按 currentCount 降序排序
            items.sort((a, b) -> Long.compare(b.getCurrentCount(), a.getCurrentCount()));
            dist.setValues(items);
            return dist;

        } catch (Exception e) {
            log.error("查询标签 {} 分布失败: {}", labelId, e.getMessage(), e);
            throw new RuntimeException("查询标签分布失败: " + e.getMessage(), e);
        }
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
     * 解析标签元数据: labelId -> DatasetField -> Dataset
     */
    private LabelMeta resolveLabelMeta(String labelId) {
        Label label = labelService.getDetailInternal(labelId);
        if (label == null) {
            log.warn("标签 {} 不存在", labelId);
            return null;
        }

        DatasetField field = datasetFieldService.getDetailByRelatedId(labelId);
        if (field == null) {
            log.warn("标签 {} 未关联数据集字段", labelId);
            return null;
        }

        Optional<Dataset> datasetOpt = datasetService.getDetail(field.getDatasetId());
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
     * 合并当前人群、对比群组的分布结果
     */
    private Map<String, long[]> mergeDistribution(List<Map<String, Object>> currentRows,
            List<Map<String, Object>> compareRows) {
        Map<String, long[]> merged = new LinkedHashMap<>();

        // 当前人群
        for (Map<String, Object> row : currentRows) {
            String value = String.valueOf(row.get("label_value"));
            long count = ((Number) row.get("cnt")).longValue();
            merged.computeIfAbsent(value, k -> new long[2]);
            merged.get(value)[0] = count;
        }

        // 对比群组
        if (compareRows != null) {
            for (Map<String, Object> row : compareRows) {
                String value = String.valueOf(row.get("label_value"));
                long count = ((Number) row.get("cnt")).longValue();
                merged.computeIfAbsent(value, k -> new long[2]);
                merged.get(value)[1] = count;
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
