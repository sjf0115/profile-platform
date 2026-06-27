package com.data.profile.web.utils;

import com.data.profile.web.model.*;
import com.data.profile.web.service.GroupService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能：规则表达式 → SQL 翻译器
 * <p>纯工具类（非 Spring Bean），将 RuleExpression DSL 翻译为 ClickHouse SQL 子查询。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
public class RuleToSqlTranslator {

    /**
     * 标签元数据：labelId → {datasetId, fieldName, entityField}
     */
    public static class LabelMeta {
        private final String datasetId;
        private final String fieldName;
        private final String entityField;

        public LabelMeta(String datasetId, String fieldName, String entityField) {
            this.datasetId = datasetId;
            this.fieldName = fieldName;
            this.entityField = entityField;
        }

        public String getDatasetId() { return datasetId; }
        public String getFieldName() { return fieldName; }
        public String getEntityField() { return entityField; }
        public String getTableName() { return "profile_dataset_" + datasetId; }
    }

    /**
     * 元数据上下文：提供规则翻译所需的元数据查找能力。
     */
    public static class MetadataContext {
        private final Map<String, LabelMeta> labelMetaMap;
        private final Map<String, String> groupTableMap;

        public MetadataContext(Map<String, LabelMeta> labelMetaMap, Map<String, String> groupTableMap) {
            this.labelMetaMap = labelMetaMap != null ? labelMetaMap : Collections.emptyMap();
            this.groupTableMap = groupTableMap != null ? groupTableMap : Collections.emptyMap();
        }

        public LabelMeta resolveLabelMeta(String labelId) {
            LabelMeta meta = labelMetaMap.get(labelId);
            if (meta == null) {
                throw new IllegalStateException("标签元数据未找到: labelId=" + labelId);
            }
            return meta;
        }

        public String resolveGroupTable(String groupId) {
            String table = groupTableMap.get(groupId);
            if (table == null) {
                return "profile_group_" + groupId;
            }
            return table;
        }
    }

    /**
     * 将 RuleExpression 翻译为 SQL 子查询。
     * 返回的 SQL 格式: SELECT DISTINCT entity_id FROM ...
     *
     * @param expression 规则表达式
     * @param context    元数据上下文
     * @return SQL 子查询字符串
     */
    public static String translate(RuleExpression expression, MetadataContext context) {
        if (expression == null || expression.getRuleGroups() == null || expression.getRuleGroups().isEmpty()) {
            throw new IllegalArgumentException("规则表达式不能为空");
        }

        String expressionLogic = normalizeLogic(expression.getLogic());
        String combiner = "AND".equals(expressionLogic) ? " INTERSECT " : " UNION ";

        List<String> groupSqls = expression.getRuleGroups().stream()
                .map(ruleGroup -> translateRuleGroup(ruleGroup, context))
                .collect(Collectors.toList());

        if (groupSqls.size() == 1) {
            return groupSqls.get(0);
        }
        return groupSqls.stream()
                .map(sql -> "(" + sql + ")")
                .collect(Collectors.joining(combiner));
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 翻译单个 RuleGroup 为 SQL。
     */
    private static String translateRuleGroup(RuleGroup ruleGroup, MetadataContext context) {
        if (ruleGroup.getRules() == null || ruleGroup.getRules().isEmpty()) {
            throw new IllegalArgumentException("规则组中没有规则");
        }

        String groupLogic = normalizeLogic(ruleGroup.getLogic());
        String combiner = "AND".equals(groupLogic) ? " INTERSECT " : " UNION ";

        List<String> ruleSqls = ruleGroup.getRules().stream()
                .map(rule -> translateRule(rule, context))
                .collect(Collectors.toList());

        if (ruleSqls.size() == 1) {
            return ruleSqls.get(0);
        }
        return ruleSqls.stream()
                .map(sql -> "(" + sql + ")")
                .collect(Collectors.joining(combiner));
    }

    /**
     * 翻译单条 Rule 为 SQL 子查询。
     */
    private static String translateRule(Rule rule, MetadataContext context) {
        String type = rule.getType();
        if ("1".equals(type)) {
            // 标签规则
            return translateLabelRule(rule, context);
        } else if ("2".equals(type)) {
            // 群组规则
            return translateGroupRule(rule, context);
        } else if ("3".equals(type)) {
            throw new UnsupportedOperationException("事件规则暂未实现 (type=3)");
        } else if ("4".equals(type)) {
            throw new UnsupportedOperationException("行为序列规则暂未实现 (type=4)");
        } else {
            throw new IllegalArgumentException("未知的规则类型: " + type);
        }
    }

    /**
     * 翻译标签规则 (type=1)。
     * SQL 模式: SELECT DISTINCT {entityField} AS entity_id FROM profile_dataset_{datasetId} WHERE ...
     */
    private static String translateLabelRule(Rule rule, MetadataContext context) {
        RuleFilterExpression filterExpression = rule.getFilterExpression();
        if (filterExpression == null || filterExpression.getFilterGroups() == null
                || filterExpression.getFilterGroups().isEmpty()) {
            throw new IllegalArgumentException("标签规则的 filterExpression 不能为空");
        }

        String filterLogic = normalizeLogic(filterExpression.getLogic());

        List<String> filterGroupSqls = new ArrayList<>();
        for (RuleFilterGroup filterGroup : filterExpression.getFilterGroups()) {
            String filterGroupSql = translateFilterGroup(filterGroup, context);
            filterGroupSqls.add(filterGroupSql);
        }

        // 所有 filterGroup 产生的 SQL 都是完整的 SELECT 语句
        if (filterGroupSqls.size() == 1) {
            return filterGroupSqls.get(0);
        }
        String combiner = "AND".equals(filterLogic) ? " INTERSECT " : " UNION ";
        return filterGroupSqls.stream()
                .map(sql -> "(" + sql + ")")
                .collect(Collectors.joining(combiner));
    }

    /**
     * 翻译过滤器组：同一组内的 filters 引用同一张表，用 AND/OR 组合为 WHERE 子句。
     */
    private static String translateFilterGroup(RuleFilterGroup filterGroup, MetadataContext context) {
        if (filterGroup.getFilters() == null || filterGroup.getFilters().isEmpty()) {
            throw new IllegalArgumentException("过滤器组中没有过滤条件");
        }

        String logic = normalizeLogic(filterGroup.getLogic());

        // 按 datasetId 分组，同一数据集的 filter 合并到一条 SQL
        Map<String, List<RuleFilter>> filtersByDataset = new LinkedHashMap<>();
        Map<String, LabelMeta> resolvedMetas = new HashMap<>();

        for (RuleFilter filter : filterGroup.getFilters()) {
            if (filter.getType() == 1) {
                // 标签过滤
                LabelMeta meta = context.resolveLabelMeta(filter.getId());
                resolvedMetas.put(filter.getId(), meta);
                filtersByDataset.computeIfAbsent(meta.getDatasetId(), k -> new ArrayList<>()).add(filter);
            } else if (filter.getType() == 2) {
                // 群组过滤 - 作为独立子查询
                filtersByDataset.computeIfAbsent("__group__" + filter.getId(), k -> new ArrayList<>()).add(filter);
            } else {
                throw new UnsupportedOperationException("不支持的过滤类型: " + filter.getType());
            }
        }

        List<String> subQueries = new ArrayList<>();
        for (Map.Entry<String, List<RuleFilter>> entry : filtersByDataset.entrySet()) {
            String key = entry.getKey();
            List<RuleFilter> filters = entry.getValue();

            if (key.startsWith("__group__")) {
                // 群组过滤器
                String groupId = filters.get(0).getId();
                String groupTable = context.resolveGroupTable(groupId);
                subQueries.add("SELECT DISTINCT entity_id FROM " + groupTable);
            } else {
                // 标签过滤器 - 合并为一条带 WHERE 的查询
                String datasetId = key;
                LabelMeta firstMeta = resolvedMetas.get(filters.get(0).getId());
                String entityField = firstMeta.getEntityField();
                String tableName = firstMeta.getTableName();

                List<String> conditions = new ArrayList<>();
                for (RuleFilter filter : filters) {
                    LabelMeta meta = resolvedMetas.get(filter.getId());
                    String condition = buildCondition(meta.getFieldName(), filter.getOp(), filter.getValues());
                    conditions.add(condition);
                }

                String whereClause = conditions.stream()
                        .collect(Collectors.joining(" " + logic + " "));

                subQueries.add(String.format("SELECT DISTINCT %s AS entity_id FROM %s WHERE %s",
                        entityField, tableName, whereClause));
            }
        }

        if (subQueries.size() == 1) {
            return subQueries.get(0);
        }
        String combiner = "AND".equals(logic) ? " INTERSECT " : " UNION ";
        return subQueries.stream()
                .map(sql -> "(" + sql + ")")
                .collect(Collectors.joining(combiner));
    }

    /**
     * 翻译群组规则 (type=2)。
     * SQL 模式: SELECT DISTINCT entity_id FROM profile_group_{groupId}
     * 支持多个群组引用，通过 filterExpression.logic 决定 INTERSECT/UNION 组合。
     */
    private static String translateGroupRule(Rule rule, MetadataContext context) {
        RuleFilterExpression filterExpression = rule.getFilterExpression();
        if (filterExpression == null || filterExpression.getFilterGroups() == null
                || filterExpression.getFilterGroups().isEmpty()) {
            throw new IllegalArgumentException("群组规则的 filterExpression 不能为空");
        }

        String filterLogic = normalizeLogic(filterExpression.getLogic());
        List<String> subQueries = new ArrayList<>();

        for (RuleFilterGroup filterGroup : filterExpression.getFilterGroups()) {
            if (filterGroup.getFilters() == null) continue;
            for (RuleFilter filter : filterGroup.getFilters()) {
                if (filter.getType() == 2) {
                    String groupId = filter.getId();
                    String groupTable = context.resolveGroupTable(groupId);
                    subQueries.add("SELECT DISTINCT entity_id FROM " + groupTable);
                }
            }
        }

        if (subQueries.isEmpty()) {
            throw new IllegalArgumentException("群组规则中没有指定群组");
        }
        if (subQueries.size() == 1) {
            return subQueries.get(0);
        }
        String combiner = "AND".equals(filterLogic) ? " INTERSECT " : " UNION ";
        return subQueries.stream()
                .map(sql -> "(" + sql + ")")
                .collect(Collectors.joining(combiner));
    }

    /**
     * 构建单个条件表达式。
     */
    private static String buildCondition(String fieldName, String op, List<String> values) {
        if (op == null) {
            throw new IllegalArgumentException("操作符不能为空, fieldName=" + fieldName);
        }
        switch (op) {
            case "eq":
                return fieldName + " = " + quote(requireFirstValue(values, fieldName, op));
            case "ne":
                return fieldName + " != " + quote(requireFirstValue(values, fieldName, op));
            case "gt":
                return fieldName + " > " + quote(requireFirstValue(values, fieldName, op));
            case "gte":
                return fieldName + " >= " + quote(requireFirstValue(values, fieldName, op));
            case "lt":
                return fieldName + " < " + quote(requireFirstValue(values, fieldName, op));
            case "lte":
                return fieldName + " <= " + quote(requireFirstValue(values, fieldName, op));
            case "contains":
                return fieldName + " LIKE '%" + escapeLike(requireFirstValue(values, fieldName, op)) + "%'";
            case "not_contains":
                return fieldName + " NOT LIKE '%" + escapeLike(requireFirstValue(values, fieldName, op)) + "%'";
            case "is_null":
                return fieldName + " IS NULL";
            case "is_not_null":
                return fieldName + " IS NOT NULL";
            case "in":
                return fieldName + " IN (" + quoteList(values, fieldName) + ")";
            case "not_in":
                return fieldName + " NOT IN (" + quoteList(values, fieldName) + ")";
            default:
                throw new IllegalArgumentException("不支持的操作符: " + op);
        }
    }

    // -------------------------------------------------------------------------
    // 辅助方法
    // -------------------------------------------------------------------------

    private static String normalizeLogic(String logic) {
        if (logic == null || logic.trim().isEmpty()) {
            return "AND";
        }
        return logic.trim().toUpperCase();
    }

    private static String requireFirstValue(List<String> values, String fieldName, String op) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("操作符 '%s' 要求至少一个值, fieldName=%s", op, fieldName));
        }
        return values.get(0);
    }

    private static String quote(String value) {
        return "'" + escape(value) + "'";
    }

    private static String quoteList(List<String> values, String fieldName) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(
                    "IN/NOT IN 操作符要求至少一个值, fieldName=" + fieldName);
        }
        return values.stream()
                .map(RuleToSqlTranslator::quote)
                .collect(Collectors.joining(", "));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        // 先转义反斜杠，再转义单引号，顺序不可颠倒
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    /**
     * 为 LIKE 模式转义元字符。
     * 在通用 SQL 转义基础上额外转义 % 和 _，避免用户输入被解释为 LIKE 通配符。
     */
    private static String escapeLike(String value) {
        if (value == null) {
            return "";
        }
        return escape(value).replace("%", "\\%").replace("_", "\\_");
    }
}