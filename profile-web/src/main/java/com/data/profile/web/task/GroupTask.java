package com.data.profile.web.task;

import com.data.profile.web.model.*;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.engine.ScheduleEngineService;
import com.data.profile.web.service.DatasetFieldService;
import com.data.profile.web.service.DatasetService;
import com.data.profile.web.service.GroupService;
import com.data.profile.web.service.TaskService;
import com.data.profile.web.utils.RuleToSqlTranslator;
import com.data.profile.web.utils.RuleToSqlTranslator.LabelMeta;
import com.data.profile.web.utils.RuleToSqlTranslator.MetadataContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 功能：群组计算任务
 * <p>负责群组圈选执行（规则翻译、预估人数、全量圈选、结果持久化）及调度配置。</p>
 * <p>业界 CDP 标准实践：CRUD 服务(GroupService) 与计算任务(GroupTask) 分离。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class GroupTask {
    @Resource
    private GroupService groupService;
    @Resource
    private DatasetService datasetService;
    @Resource
    private DatasetFieldService datasetFieldService;
    @Resource
    private AnalysisEngineService analysisEngineService;
    @Resource
    private ScheduleEngineService scheduleEngineService;
    @Resource
    private TaskService taskService;

    /**
     * 预估群组人数（前端交互式调用，不走 Task 体系）。
     * 翻译 GroupRule → COUNT SQL → 执行查询 → 返回人数。
     *
     * @param groupRule           群组规则
     * @param entityIdentifierId  实体标识ID
     * @return 预估人数
     */
    public long estimateGroupCount(GroupRule groupRule, String entityIdentifierId) {
        // 1. 校验规则
        if (groupRule == null || !"rule".equals(groupRule.getType())) {
            throw new IllegalArgumentException("仅支持规则类型(rule)的群组进行预估");
        }
        RuleExpression expression = groupRule.getExpression();
        if (expression == null || expression.getRuleGroups() == null || expression.getRuleGroups().isEmpty()) {
            throw new IllegalArgumentException("规则表达式不能为空");
        }

        // 2. 构建元数据上下文
        MetadataContext context = buildMetadataContext(expression);

        // 3. 翻译规则为 SQL
        String subQuery = RuleToSqlTranslator.translate(expression, context);
        log.info("群组预估 - 翻译 SQL: {}", subQuery);

        // 4. 包装为 COUNT 查询
        String countSql = "SELECT COUNT(DISTINCT entity_id) FROM (" + subQuery + ")";
        log.info("群组预估 - 执行 COUNT SQL: {}", countSql);

        // 5. 执行查询
        try {
            long count = analysisEngineService.executeCountQuery(countSql);
            log.info("群组预估结果: {} 人", count);
            return count;
        } catch (Exception e) {
            log.error("群组预估执行失败", e);
            throw new RuntimeException("群组预估执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行群组圈选（Task dispatch 调用）。
     * 翻译规则 → 写临时表 → EXCHANGE TABLES 原子交换 → 更新 groupCount。
     *
     * @param groupId 群组ID
     */
    public void executeGroupSelection(String groupId) {
        log.info("开始执行群组圈选: groupId={}", groupId);

        // 1. 加载群组元数据
        Optional<Group> groupOpt = groupService.getDetail(groupId);
        if (!groupOpt.isPresent()) {
            throw new RuntimeException("群组不存在: " + groupId);
        }
        Group group = groupOpt.get();
        GroupRule groupRule = group.getGroupRule();

        if (groupRule == null || !"rule".equals(groupRule.getType())) {
            throw new IllegalStateException("群组 " + groupId + " 不是规则类型，无法执行圈选");
        }
        RuleExpression expression = groupRule.getExpression();
        if (expression == null || expression.getRuleGroups() == null || expression.getRuleGroups().isEmpty()) {
            throw new IllegalStateException("群组 " + groupId + " 规则表达式为空");
        }

        // 2. 自引用检测：禁止群组规则中引用自身
        Set<String> referencedGroupIds = collectReferencedGroupIds(expression);
        if (referencedGroupIds.contains(groupId)) {
            throw new IllegalStateException("群组 " + groupId + " 存在自引用，无法执行圈选");
        }

        // 3. 验证被引用群组的结果表存在（前置防御）
        for (String refGroupId : referencedGroupIds) {
            String refTable = GroupService.GROUP_TABLE_PREFIX + refGroupId;
            String checkSql = "EXISTS TABLE " + refTable;
            try {
                long exists = analysisEngineService.executeCountQuery(checkSql);
                if (exists == 0) {
                    throw new IllegalStateException(
                            "被引用群组 " + refGroupId + " 尚未执行圈选，请先执行该群组");
                }
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                log.warn("检查引用群组表失败: {}", refGroupId, e);
            }
        }

        // 4. 构建元数据上下文
        MetadataContext context = buildMetadataContext(expression);

        // 5. 翻译规则为 SQL
        String subQuery = RuleToSqlTranslator.translate(expression, context);
        log.info("群组圈选 - 翻译 SQL: groupId={}, sql={}", groupId, subQuery);

        String resultTable = GroupService.GROUP_TABLE_PREFIX + groupId;
        String tmpTable = resultTable + "_tmp";

        try {
            // 6. 建临时表（先 DROP 再 CREATE，确保干净）
            analysisEngineService.executeStatement("DROP TABLE IF EXISTS " + tmpTable);
            analysisEngineService.executeStatement(buildCreateTableSql(tmpTable));

            // 7. 写入圈选结果到临时表
            String insertSql = String.format(
                    "INSERT INTO %s (entity_id) SELECT DISTINCT entity_id FROM (%s)",
                    tmpTable, subQuery);
            analysisEngineService.executeStatement(insertSql);
            log.info("群组圈选 - 数据写入临时表完成: {}", tmpTable);

            // 8. 原子交换（ClickHouse EXCHANGE TABLES 原子操作，其他读者无感知）
            String exchangeSql = String.format("EXCHANGE TABLES %s AND %s", resultTable, tmpTable);
            analysisEngineService.executeStatement(exchangeSql);
            log.info("群组圈选 - 原子交换完成: {} <-> {}", resultTable, tmpTable);

            // 9. 清理旧临时表（交换后里面是旧数据）
            analysisEngineService.executeStatement("DROP TABLE IF EXISTS " + tmpTable);

            // 10. 查询结果数量
            String countSql = "SELECT COUNT(*) FROM " + resultTable;
            long count = analysisEngineService.executeCountQuery(countSql);
            log.info("群组圈选 - 圈选人数: groupId={}, count={}", groupId, count);

            // 11. 回写 groupCount（防止溢出）
            int groupCount = count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
            groupService.updateGroupCount(groupId, groupCount);
            log.info("群组圈选完成: groupId={}, count={}", groupId, count);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("群组圈选执行失败: groupId={}", groupId, e);
            throw new RuntimeException("群组圈选执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 配置群组调度
     * <p>通过 ScheduleEngineService 注册/更新调度到调度引擎。</p>
     *
     * @param groupId    群组ID
     * @param triggerType 调度类型
     * @param cron       Cron 表达式
     * @param startTime  生效开始时间
     * @param endTime    生效结束时间
     */
    public void schedule(String groupId, int triggerType, String cron, String startTime, String endTime) {
        Task task = taskService.getDetailByRelatedId(groupId);
        if (task == null) {
            log.error("群组 [{}] 没有关联的圈选任务，无法配置调度", groupId);
            throw new RuntimeException("群组没有关联的圈选任务，请先创建群组");
        }
        scheduleEngineService.configureSchedule(
                task.getTaskId(), triggerType, cron, startTime, endTime);
        log.info("群组 [{}] 调度配置完成: triggerType={}", groupId, triggerType);
    }

    /**
     * 获取群组关联的调度任务配置
     */
    public Task getSchedulerConfig(String groupId) {
        return taskService.getDetailByRelatedId(groupId);
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 构建结果表 CREATE TABLE 语句。
     */
    private String buildCreateTableSql(String tableName) {
        return String.format(
                "CREATE TABLE IF NOT EXISTS %s (" +
                        "entity_id String COMMENT '实体ID', " +
                        "_created_time DateTime DEFAULT now() COMMENT '圈选时间'" +
                        ") ENGINE = MergeTree() ORDER BY entity_id SETTINGS index_granularity = 8192",
                tableName);
    }

    /**
     * 收集 RuleExpression 中所有被引用的群组 ID（type=2 的 filter）。
     */
    private Set<String> collectReferencedGroupIds(RuleExpression expression) {
        Set<String> groupIds = new HashSet<>();
        if (expression == null || expression.getRuleGroups() == null) {
            return groupIds;
        }
        for (RuleGroup ruleGroup : expression.getRuleGroups()) {
            if (ruleGroup.getRules() == null) continue;
            for (Rule rule : ruleGroup.getRules()) {
                RuleFilterExpression fe = rule.getFilterExpression();
                if (fe == null || fe.getFilterGroups() == null) continue;
                for (RuleFilterGroup fg : fe.getFilterGroups()) {
                    if (fg.getFilters() == null) continue;
                    for (RuleFilter f : fg.getFilters()) {
                        if (f.getType() == 2 && f.getId() != null) {
                            groupIds.add(f.getId());
                        }
                    }
                }
            }
        }
        return groupIds;
    }

    /**
     * 构建 MetadataContext：遍历 RuleExpression 中的所有 Rule，
     * 收集标签 ID 和群组 ID 对应的元数据。
     */
    private MetadataContext buildMetadataContext(RuleExpression expression) {
        Map<String, LabelMeta> labelMetaMap = new HashMap<>();
        Map<String, String> groupTableMap = new HashMap<>();

        for (RuleGroup ruleGroup : expression.getRuleGroups()) {
            if (ruleGroup.getRules() == null) continue;
            for (Rule rule : ruleGroup.getRules()) {
                collectMetadata(rule, labelMetaMap, groupTableMap);
            }
        }
        return new MetadataContext(labelMetaMap, groupTableMap);
    }

    /**
     * 从单条规则中收集元数据。
     */
    private void collectMetadata(Rule rule, Map<String, LabelMeta> labelMetaMap, Map<String, String> groupTableMap) {
        RuleFilterExpression filterExpression = rule.getFilterExpression();
        if (filterExpression == null || filterExpression.getFilterGroups() == null) return;

        for (RuleFilterGroup filterGroup : filterExpression.getFilterGroups()) {
            if (filterGroup.getFilters() == null) continue;
            for (RuleFilter filter : filterGroup.getFilters()) {
                if (filter.getType() == 1) {
                    // 标签：通过 labelId 查找 datasetField → dataset
                    String labelId = filter.getId();
                    if (!labelMetaMap.containsKey(labelId)) {
                        LabelMeta meta = resolveLabelMeta(labelId);
                        if (meta != null) {
                            labelMetaMap.put(labelId, meta);
                        }
                    }
                } else if (filter.getType() == 2) {
                    // 群组
                    String groupId = filter.getId();
                    groupTableMap.putIfAbsent(groupId, GroupService.GROUP_TABLE_PREFIX + groupId);
                }
            }
        }
    }

    /**
     * 通过 labelId 解析标签元数据：
     * labelId → DatasetField (relatedId) → datasetId + fieldName → Dataset (entityField)
     */
    private LabelMeta resolveLabelMeta(String labelId) {
        // 通过 relatedId 查找 DatasetField
        DatasetField field = datasetFieldService.getDetailByRelatedId(labelId);
        if (field == null) {
            log.warn("标签 {} 未关联数据集字段", labelId);
            throw new IllegalStateException("标签 " + labelId + " 未关联数据集字段，无法圈选");
        }

        // 获取数据集信息（entityField）
        String datasetId = field.getDatasetId();
        Optional<Dataset> datasetOpt = datasetService.getDetail(datasetId);
        if (!datasetOpt.isPresent()) {
            throw new IllegalStateException("数据集不存在: " + datasetId);
        }
        Dataset dataset = datasetOpt.get();
        String entityField = dataset.getEntityField();
        if (entityField == null || entityField.isEmpty()) {
            throw new IllegalStateException("数据集 " + datasetId + " 未配置实体字段(entityField)");
        }

        return new LabelMeta(datasetId, field.getFieldName(), entityField);
    }
}