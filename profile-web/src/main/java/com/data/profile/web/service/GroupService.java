package com.data.profile.web.service;

import com.beust.jcommander.internal.Lists;
import com.data.profile.common.enums.*;
import com.data.profile.web.dao.GroupMapper;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.engine.ScheduleEngineService;
import com.data.profile.web.model.*;
import com.data.profile.web.utils.RuleToSqlTranslator;
import com.data.profile.web.vo.DatasetVO;
import com.data.profile.web.vo.GroupVO;
import org.apache.commons.lang3.StringUtils;
import com.data.profile.web.security.RequestContext;
import com.data.profile.common.utils.IDGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.data.profile.common.domain.Constant.ENGINE_DATASET_TABLE_PREFIX;

/**
 * 功能：群组服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class GroupService {
    private static final Gson gson = new GsonBuilder().create();

    /** 群组引擎表前缀 */
    public static final String GROUP_TABLE_PREFIX = "profile_group_";
    @Resource
    private GroupMapper groupMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskInstanceService taskInstanceService;
    @Autowired
    private EntityIdentifierService entityIdentifierService;
    @Autowired
    private MinioService minioService;
    @Autowired
    private AnalysisEngineService analysisEngineService;
    @Autowired
    private ScheduleEngineService scheduleEngineService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private DatasetFieldService datasetFieldService;

    /**
     * 根据查询条件获取群组列表（含关联信息）
     * @param group 群组查询条件
     * @return 群组 VO 列表
     */
    public List<GroupVO> getList(Group group) {
        List<Group> groups = groupMapper.selectByParams(group);
        List<GroupVO> targets = groups.stream().map(target -> {
            GroupVO vo = toVO(target);
            // 查询最新任务实例
            TaskInstance latestInstance = taskInstanceService.getLatestByRelatedId(target.getGroupId());
            vo.setLatestInstance(latestInstance);
            return vo;
        }).collect(Collectors.toList());
        log.info("根据查询条件获取 {} 个群组", groups.size());
        return targets;
    }

    /**
     * 根据群组ID获取群组详细信息（含关联信息）
     * @param groupId 群组ID
     * @return 群组 VO
     */
    public Optional<GroupVO> getDetail(String groupId) {
        Group group = groupMapper.selectByGroupId(groupId);
        if (group == null) {
            return Optional.empty();
        }
        GroupVO vo = toVO(group);
        // 获取群组调度配置
        Task task = taskService.getDetailByRelatedId(groupId);
        if (!Objects.equals(task, null)) {
            vo.setTaskId(task.getTaskId());
            vo.setTriggerType(task.getTriggerType());
            vo.setTriggerCron(task.getTriggerCron());
            vo.setTriggerStartTime(task.getTriggerStartTime());
            vo.setTriggerEndTime(task.getTriggerEndTime());
        }
        // 查询最新任务实例
        TaskInstance latestInstance = taskInstanceService.getLatestByRelatedId(groupId);
        vo.setLatestInstance(latestInstance);
        log.info("根据群组ID获取群组详细信息: {}", gson.toJson(vo));
        return Optional.of(vo);
    }

    /**
     * 根据群组ID获取群组纯 Model（供内部调用，不含关联信息）
     * @param groupId 群组ID
     * @return 群组 Model
     */
    public Optional<Group> getDetailModel(String groupId) {
        Group group = groupMapper.selectByGroupId(groupId);
        if (group == null) {
            return Optional.empty();
        }
        return Optional.of(group);
    }

    /**
     * 将 Group Model 转换为 GroupVO（填充实体关联信息）
     */
    private GroupVO toVO(Group group) {
        GroupVO vo = new GroupVO();
        BeanUtils.copyProperties(group, vo);
        // 获取群组实体信息
        String entityIdentifierId = group.getEntityIdentifierId();
        Optional<EntityIdentifier> entityIdentifierOp = entityIdentifierService.getDetail(entityIdentifierId);
        if (entityIdentifierOp.isPresent()) {
            EntityIdentifier entityIdentifier = entityIdentifierOp.get();
            vo.setEntityId(entityIdentifier.getEntityId());
            vo.setEntityName(entityIdentifier.getEntityName());
            vo.setEntityIdentifierName(entityIdentifier.getEntityIdentifierName());
        }
        return vo;
    }

    /**
     * 创建群组
     * @param group 群组
     */
    @Transactional
    public int create(Group group) throws RuntimeException {
        String groupName = group.getGroupName();

        // 群组处理
        List<Group> groups = groupMapper.selectSimpleByGroupName(groupName);
        if (!groups.isEmpty()) {
            log.error("群组 {} 已经存在，不允许重复添加", groupName);
            throw new RuntimeException("群组已经存在，不允许重复添加");
        }
        String groupId = IDGenerator.getInstance().generate(ModelType.GROUP);
        Group target = groupMapper.selectSimpleByGroupId(groupId);
        if (!Objects.equals(target, null)) {
            log.error("群组ID {} 已经存在，不允许重复添加", groupId);
            throw new RuntimeException("群组ID已经存在，不允许重复添加");
        }
        group.setGroupId(groupId);
        group.setSourceType(SourceType.CUSTOM.getCode());
        group.setGroupStatus(Status.ENABLE.getCode());
        group.setOwner(RequestContext.currentUserId());
        group.setCreator(RequestContext.currentUserId());
        group.setModifier(RequestContext.currentUserId());

        // TODO 优化 处理上传文件类型群组
        if (group.getGroupType() != null && group.getGroupType() == 2) {
            GroupRule groupRule = group.getGroupRule();
            if (groupRule != null && "upload".equals(groupRule.getType())) {
                log.info("上传文件类型群组，MinIO 文件路径: {}", groupRule.getUuidFileKey());
            }
        }

        // TODO 优化 处理 SQL 创建类型群组
        if (group.getGroupType() != null && group.getGroupType() == 3) {
            GroupRule groupRule = group.getGroupRule();
            if (groupRule == null || !"sql".equals(groupRule.getType())
                    || StringUtils.isBlank(groupRule.getSqlText())) {
                throw new RuntimeException("SQL创建群组必须提供 SQL 语句");
            }
            String sqlText = groupRule.getSqlText().trim();
            // 安全校验：禁止危险语句
            String upperSql = sqlText.toUpperCase();
            String[] forbidden = {"DROP ", "DELETE ", "ALTER ", "TRUNCATE ", "INSERT ", "UPDATE ", "CREATE "};
            for (String kw : forbidden) {
                if (upperSql.contains(kw)) {
                    throw new RuntimeException("SQL 中不允许包含危险操作: " + kw.trim());
                }
            }
            // dry-run 校验：执行 LIMIT 0 验证语法正确性
            try {
                String dryRunSql = "SELECT entity_id FROM (" + sqlText + ") LIMIT 0";
                analysisEngineService.executeStatement(dryRunSql);
            } catch (Exception e) {
                throw new RuntimeException("SQL 语法校验失败: " + e.getMessage());
            }
            log.info("SQL创建类型群组，SQL: {}", sqlText);
        }

        // 修改时预估人数
        long count = estimateGroupCount(group.getGroupRule());
        // TODO 优化 Long -> Int
        group.setGroupCount((int)count);

        int result = groupMapper.insertSelective(group);
        log.info("新增群组: {}", gson.toJson(group));

        // 创建群组引擎表
        createGroupEngineTable(group);

        // 创建圈选任务
        createAnalysisTask(group, groupId);

        // 注册调度到调度引擎
        scheduleGroupIfNeeded(group);

        return result;
    }

    /**
     * 修改群组
     * <p>更新群组元数据，并同步调度配置到调度引擎。</p>
     *
     * @param group 群组
     */
    @Transactional
    public int update(Group group) {
        // 修改群组
        group.setModifier(RequestContext.currentUserId());
        // 修改时预估人数
        long count = estimateGroupCount(group.getGroupRule());
        // TODO 优化 Long -> Int
        group.setGroupCount((int)count);
        log.info("更新群组: {}", gson.toJson(group));
        int result = groupMapper.updateByGroupIdSelective(group);
        // TODO 优化 同步调度到调度引擎
        scheduleGroupIfNeeded(group);
        return result;
    }

    /**
     * 删除群组
     * @param groupId 群组ID
     */
    @Transactional
    public int delete(String groupId) {
        Group group = groupMapper.selectSimpleByGroupId(groupId);
        if (Objects.equals(group, null)) {
            log.error("群组 {} 不存在，无法删除", groupId);
            throw new RuntimeException("群组不存在，无法删除");
        }
        if (Objects.equals(group.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置群组 {} 不允许删除", groupId);
            throw new RuntimeException("内置群组不允许删除");
        }

        // 删除调度任务
        taskService.deleteByRelatedId(groupId);

        // 删除群组引擎表
        dropGroupEngineTable(groupId);

        // 删除群组元数据
        log.info("删除群组: {}", groupId);
        return groupMapper.deleteByGroupId(groupId);
    }

    /**
     * 上传文件到 MinIO
     * @param file 文件
     */
    public GroupRule upload (MultipartFile file) {
        log.info("请求上传文件: {}", file.getOriginalFilename());
        // 1. 验证文件
        if (file.isEmpty()) {
            log.error("上传文件不能为空");
            throw new RuntimeException("上传文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            log.error("仅支持 CSV 格式的文件");
            throw new RuntimeException("仅支持 CSV 格式的文件");
        }
        if (file.getSize() > 200 * 1024 * 1024L) {
            log.error("文件大小不能超过 200M");
            throw new RuntimeException("文件大小不能超过 200M");
        }
        // 2. 上传到 MinIO
        String objectName = minioService.uploadFile(file, "upload_group");
        // 3. 构建返回结果
        GroupRule rule = GroupRule.builder()
                .uuidFileKey(objectName)
                .fileList(Lists.newArrayList(filename))
                .build();
        log.info("文件上传成功: {}", objectName);
        return rule;
    }

    /**
     * 取消上传
     */
    public void cancelUpload (String fileKey) {
        try {
            minioService.deleteFile(fileKey);
        } catch (Exception e) {
            log.error("取消上传删除文件失败: {}", e.getMessage());
            throw new RuntimeException("取消上传删除文件失败");
        }
    }

    /**
     * 更新群组人数
     * @param groupId 群组ID
     * @param count   圈选人数
     */
    public void updateGroupCount(String groupId, int count) {
        groupMapper.updateGroupCount(groupId, count);
    }

    /**
     * 获取 SQL 创建可用的数据集表和字段列表
     * <p>查询已就绪的数据集（含字段），直接返回 DatasetVO 列表。</p>
     *
     * @param entityIdentifierId 实体标识ID
     */
    public List<DatasetVO> getAvailableTables(String entityIdentifierId) {
        Dataset query = new Dataset();
        // entityIdentifierId 实际为实体标识ID
        query.setEntityId(entityIdentifierId);
        query.setStatus(1);
        return datasetService.getListWithFields(query);
    }

    /**
     * 预估群组人数
     * @param groupRule 群组规则
     * @return 预估人数
     */
    public long estimateGroupCount(GroupRule groupRule) {
        if (groupRule == null) {
            throw new IllegalArgumentException("群组规则不能为空");
        }
        String ruleType = groupRule.getType();
        if ("sql".equals(ruleType)) {
            // SQL 创建群组预估
            return estimateSqlGroupCount(groupRule.getSqlText());
        } else if ("rule".equals(ruleType)) {
            // 规则创建群组预估
            return estimateRuleGroupCount(groupRule);
        } else {
            // 其它
            throw new IllegalArgumentException("仅支持规则创建和SQL创建的群组进行预估");
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * SQL创建群组预估人数
     * @param sqlText 执行SQL
     * @return 预估人数
     */
    private long estimateSqlGroupCount(String sqlText) {
        if (StringUtils.isBlank(sqlText)) {
            throw new IllegalArgumentException("执行 SQL 不能为空");
        }
        String countSql = "SELECT COUNT(DISTINCT entity_id) FROM (" + sqlText + ")";
        log.info("群组预估(SQL) - 执行: {}", countSql);
        try {
            long count = analysisEngineService.executeCountQuery(countSql);
            log.info("SQL创建群组预估结果: {}", count);
            return count;
        } catch (Exception e) {
            log.error("SQL创建群组预估执行失败", e);
            throw new RuntimeException("SQL创建群组预估执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 规则创建群组预估人数
     * @param groupRule 规则
     * @return 预估人数
     */
    public long estimateRuleGroupCount(GroupRule groupRule) {
        RuleExpression expression = groupRule.getExpression();
        if (expression == null || expression.getRuleGroups() == null || expression.getRuleGroups().isEmpty()) {
            throw new IllegalArgumentException("规则表达式不能为空");
        }

        // 2. 构建元数据上下文
        RuleToSqlTranslator.MetadataContext context = buildMetadataContext(expression);

        // 3. 翻译规则为 SQL
        String subQuery = RuleToSqlTranslator.translate(expression, context);
        log.info("群组预估 - 翻译 SQL: {}", subQuery);

        // 4. 包装为 COUNT 查询
        String countSql = "SELECT COUNT(DISTINCT entity_id) FROM (" + subQuery + ")";
        log.info("群组预估 - 执行 COUNT SQL: {}", countSql);

        // 5. 执行查询
        try {
            long count = analysisEngineService.executeCountQuery(countSql);
            log.info("规则创建群组预估结果: {}", count);
            return count;
        } catch (Exception e) {
            log.error("规则创建群组预估执行失败", e);
            throw new RuntimeException("规则创建群组预估执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * TODO 待优化
     * 构建 MetadataContext：遍历 RuleExpression 中的所有 Rule，
     * 收集标签 ID 和群组 ID 对应的元数据。
     */
    public RuleToSqlTranslator.MetadataContext buildMetadataContext(RuleExpression expression) {
        Map<String, RuleToSqlTranslator.LabelMeta> labelMetaMap = new HashMap<>();
        Map<String, String> groupTableMap = new HashMap<>();

        for (RuleGroup ruleGroup : expression.getRuleGroups()) {
            if (ruleGroup.getRules() == null) continue;
            for (Rule rule : ruleGroup.getRules()) {
                collectMetadata(rule, labelMetaMap, groupTableMap);
            }
        }
        return new RuleToSqlTranslator.MetadataContext(labelMetaMap, groupTableMap);
    }

    /**
     * TODO 待优化
     * 从单条规则中收集元数据。
     */
    private void collectMetadata(Rule rule, Map<String, RuleToSqlTranslator.LabelMeta> labelMetaMap, Map<String, String> groupTableMap) {
        RuleFilterExpression filterExpression = rule.getFilterExpression();
        if (filterExpression == null || filterExpression.getFilterGroups() == null) return;

        for (RuleFilterGroup filterGroup : filterExpression.getFilterGroups()) {
            if (filterGroup.getFilters() == null) continue;
            for (RuleFilter filter : filterGroup.getFilters()) {
                if (filter.getType() == 1) {
                    // 标签：通过 labelId 查找 datasetField → dataset
                    String labelId = filter.getId();
                    if (!labelMetaMap.containsKey(labelId)) {
                        RuleToSqlTranslator.LabelMeta meta = resolveLabelMeta(labelId);
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
     * TODO 待优化
     * 通过 labelId 解析标签元数据：
     * labelId → DatasetField (relatedId) → datasetId + fieldName → Dataset (entityField)
     */
    private RuleToSqlTranslator.LabelMeta resolveLabelMeta(String labelId) {
        // 数据集字段
        DatasetField field = datasetFieldService.getDetailByRelatedId(labelId);
        if (field == null) {
            log.error("标签 {} 未关联数据集字段", labelId);
            throw new IllegalStateException("标签 " + labelId + " 未关联数据集字段，无法圈选");
        }

        // 数据集信息
        String datasetId = field.getDatasetId();
        Optional<Dataset> datasetOpt = datasetService.getDetailModel(datasetId);
        if (!datasetOpt.isPresent()) {
            throw new IllegalStateException("数据集不存在: " + datasetId);
        }
        Dataset dataset = datasetOpt.get();
        String entityField = dataset.getEntityField();
        if (entityField == null || entityField.isEmpty()) {
            throw new IllegalStateException("数据集 " + datasetId + " 未配置实体字段(entityField)");
        }

        return new RuleToSqlTranslator.LabelMeta(datasetId, field.getFieldName(), entityField);
    }

    /**
     * 创建圈选任务
     * <p>仅创建 Task 元数据，调度注册由 GroupController 调用 GroupTask.schedule() 完成。</p>
     */
    private void createAnalysisTask(Group group, String groupId) {
        Task task = Task.builder()
                .taskName(group.getGroupName())
                .taskType(TaskType.GROUP.getCode())
                .taskDesc(group.getGroupName() + "群组圈选任务")
                .taskRelatedId(groupId)
                .build();
        taskService.create(task);
        log.info("为群组 [{}] 创建圈选任务", groupId);
    }

    /**
     * 创建群组引擎表
     * <p>在分析引擎中创建群组结果表，用于存储圈选结果。</p>
     * <p>建表失败不阻塞群组创建流程。</p>
     */
    private void createGroupEngineTable(Group group) {
        String tableName = GROUP_TABLE_PREFIX + group.getGroupId();
        String createSql = String.format(
                "CREATE TABLE IF NOT EXISTS %s (" +
                        "entity_id String COMMENT '实体ID', " +
                        "_created_time DateTime DEFAULT now() COMMENT '圈选时间'" +
                        ") ENGINE = MergeTree() ORDER BY entity_id SETTINGS index_granularity = 8192",
                tableName);
        try {
            analysisEngineService.executeStatement(createSql);
            log.info("群组引擎表创建成功: {}", tableName);
        } catch (Exception e) {
            log.warn("群组引擎表创建失败（非阻塞）: table={}, reason={}", tableName, e.getMessage());
        }
    }

    /**
     * 删除群组引擎表
     * <p>从分析引擎中删除群组结果表。</p>
     */
    private void dropGroupEngineTable(String groupId) {
        String tableName = GROUP_TABLE_PREFIX + groupId;
        try {
            analysisEngineService.executeStatement("DROP TABLE IF EXISTS " + tableName);
            log.info("群组引擎表删除成功: {}", tableName);
        } catch (Exception e) {
            log.warn("群组引擎表删除失败（非阻塞）: table={}, reason={}", tableName, e.getMessage());
        }
    }

    /**
     * 如果群组配置了调度，则注册到调度引擎。
     * <p>直接调用 ScheduleEngineService，避免循环依赖。</p>
     * <p>调度注册失败不阻塞群组创建/更新流程。</p>
     */
    private void scheduleGroupIfNeeded(Group group) {
        if (group.getTriggerType() != null) {
            try {
                Task task = taskService.getDetailByRelatedId(group.getGroupId());
                if (task == null) {
                    log.warn("群组 [{}] 没有关联的圈选任务，无法注册调度", group.getGroupId());
                    return;
                }
                scheduleEngineService.configureSchedule(
                        task.getTaskId(),
                        group.getTriggerType(),
                        group.getTriggerCron(),
                        group.getTriggerStartTime(),
                        group.getTriggerEndTime());
                log.info("群组 [{}] 调度注册成功: triggerType={}", group.getGroupId(), group.getTriggerType());
            } catch (Exception e) {
                log.warn("群组调度注册失败（非阻塞）: groupId={}, reason={}", group.getGroupId(), e.getMessage());
            }
        }
    }
}