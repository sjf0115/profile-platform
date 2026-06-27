package com.data.profile.web.task;

import com.data.profile.web.model.*;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.engine.ScheduleEngineService;
import com.data.profile.web.service.DatasetFieldService;
import com.data.profile.web.service.DatasetService;
import com.data.profile.web.service.GroupService;
import com.data.profile.web.service.TaskService;
import com.data.profile.web.utils.RuleToSqlTranslator;
import com.data.profile.web.utils.RuleToSqlTranslator.MetadataContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
     * 执行群组圈选（Task dispatch 调用）。
     * 翻译规则 → 写临时表 → EXCHANGE TABLES 原子交换 → 更新 groupCount。
     *
     * @param groupId 群组ID
     */
    public void executeGroupSelection(String groupId) {
        log.info("开始执行群组圈选: groupId={}", groupId);

        // 1. 加载群组元数据
        Optional<Group> groupOpt = groupService.getDetailModel(groupId);
        if (!groupOpt.isPresent()) {
            throw new RuntimeException("群组不存在: " + groupId);
        }
        Group group = groupOpt.get();
        GroupRule groupRule = group.getGroupRule();

        if (groupRule == null) {
            throw new IllegalStateException("群组 " + groupId + " 规则为空，无法执行圈选");
        }

        String subQuery;
        if ("rule".equals(groupRule.getType())) {
            RuleExpression expression = groupRule.getExpression();
            if (expression == null || expression.getRuleGroups() == null || expression.getRuleGroups().isEmpty()) {
                throw new IllegalStateException("群组 " + groupId + " 规则表达式为空");
            }

            // 自引用检测：禁止群组规则中引用自身
            Set<String> referencedGroupIds = collectReferencedGroupIds(expression);
            if (referencedGroupIds.contains(groupId)) {
                throw new IllegalStateException("群组 " + groupId + " 存在自引用，无法执行圈选");
            }

            // 验证被引用群组的结果表存在（前置防御）
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

            // 构建元数据上下文
            MetadataContext context = groupService.buildMetadataContext(expression);
            // 翻译规则为 SQL
            subQuery = RuleToSqlTranslator.translate(expression, context);
            log.info("群组圈选 - 翻译 SQL: groupId={}, sql={}", groupId, subQuery);
        } else if ("sql".equals(groupRule.getType())) {
            subQuery = groupRule.getSqlText();
            if (StringUtils.isBlank(subQuery)) {
                throw new IllegalStateException("群组 " + groupId + " 的 SQL 为空");
            }
            log.info("群组圈选 - 使用自定义 SQL: groupId={}", groupId);
        } else {
            throw new IllegalStateException("群组 " + groupId + " 不支持的规则类型: " + groupRule.getType());
        }

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
}