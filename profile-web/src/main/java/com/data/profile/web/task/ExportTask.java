package com.data.profile.web.task;

import com.data.profile.web.dao.ExportMapper;
import com.data.profile.web.dto.DataSourceDTO;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.model.Application;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Export;
import com.data.profile.web.model.ExportConfig;
import com.data.profile.web.service.ApplicationService;
import com.data.profile.web.service.DataSourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import static com.data.profile.common.domain.Constant.ENGINE_GROUP_TABLE_PREFIX;

/**
 * 功能：群组投递计算任务
 * <p>负责投递执行（配置解析、目标类型推断、多目标投递）。</p>
 * <p>CRUD 服务(ExportService) 与计算任务(ExportTask) 分离。</p>
 * <p>目标类型基于数据源 datasource_type 推断，database/bucket/topic 从数据源 config 获取。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class ExportTask {

    private static final Gson gson = new Gson();
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // 数据源类型 → 目标类型映射
    private static final List<String> TABLE_TYPES = Arrays.asList("mysql", "clickhouse", "postgresql", "oracle", "hive", "doris", "jdbc");
    private static final List<String> FILE_TYPES = Arrays.asList("minio", "hdfs", "oss", "s3");
    private static final List<String> TOPIC_TYPES = Arrays.asList("kafka", "rabbitmq", "rocketmq");
    private static final List<String> INDEX_TYPES = Arrays.asList("elasticsearch", "es");

    @Resource
    private ExportMapper exportMapper;
    @Resource
    private ApplicationService applicationService;
    @Resource
    private DataSourceService dataSourceService;
    @Resource
    private AnalysisEngineService analysisEngineService;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 执行投递
     *
     * @param exportId 投递ID
     */
    public void executeExport(String exportId) throws Exception {
        log.info("开始执行投递: exportId={}", exportId);

        // 1. 加载投递配置
        Export export = exportMapper.selectByExportId(exportId);
        if (export == null) {
            throw new RuntimeException("投递不存在: " + exportId);
        }

        // 2. 解析实际目标配置
        ExportConfig targetConfig;
        String datasourceType;
        if (export.getExportMode() == 2) {
            // 应用投递：从 Application.targetConfig 获取目标配置
            targetConfig = resolveApplicationTargetConfig(export);
            // 应用投递的目标类型需要从应用配置推断（暂不支持）
            datasourceType = null;
        } else {
            // 数据源投递：直接使用 export_config
            targetConfig = parseExportConfig(export.getExportConfig());
            if (targetConfig == null) {
                throw new RuntimeException("投递配置解析失败: " + exportId);
            }
            // 从数据源获取类型
            DataSourceDTO ds = dataSourceService.getDetail(targetConfig.getDatasourceId());
            datasourceType = ds.getDatasourceType();
        }

        // 3. 推断目标类型
        String targetType = resolveTargetType(datasourceType, targetConfig);
        log.info("投递目标类型: exportId={}, targetType={}", exportId, targetType);

        // 4. 构建源表
        String groupId = getGroupIdFromConfig(export);
        String sourceTable = ENGINE_GROUP_TABLE_PREFIX + groupId;

        // 5. 按目标类型分发执行
        switch (targetType) {
            case "table":
                executeTableExport(exportId, groupId, sourceTable, targetConfig, datasourceType);
                break;
            case "file":
                executeFileExport(exportId, groupId, sourceTable, targetConfig, datasourceType);
                break;
            case "topic":
                executeTopicExport(exportId, groupId, sourceTable, targetConfig, datasourceType);
                break;
            default:
                throw new IllegalArgumentException("不支持的目标类型: " + targetType);
        }

        log.info("投递执行完成: exportId={}", exportId);
    }

    // =================================================================================================================
    // 目标类型执行方法
    // =================================================================================================================

    /**
     * 数据表投递：支持 MySQL/ClickHouse/Hive/Doris 等
     * <p>从数据源 config 获取 database，从 export_config 获取 tableName/writeMode/targetColumn。</p>
     */
    private void executeTableExport(String exportId, String groupId, String sourceTable,
                                    ExportConfig config, String datasourceType) throws Exception {
        // 从数据源 config 提取 database
        DataSourceDTO ds = dataSourceService.getDetail(config.getDatasourceId());
        String database = extractFieldFromConfig(ds.getConfig(), "database");
        String targetTable = database + "." + config.getTableName();
        String writeMode = config.getWriteMode();
        String targetColumn = config.getTargetColumn();

        log.info("开始执行数据表投递: exportId={}, targetTable={}, writeMode={}, targetColumn={}",
                exportId, targetTable, writeMode, targetColumn);

        if ("upsert".equalsIgnoreCase(writeMode) && StringUtils.isNotBlank(targetColumn)) {
            // 覆盖模式：先删除已存在数据，再插入
            String deleteSql = String.format(
                    "ALTER TABLE %s DELETE WHERE %s IN (SELECT %s FROM %s)",
                    targetTable, targetColumn, targetColumn, sourceTable);
            log.info("覆盖模式 - 删除已存在数据: {}", deleteSql);
            analysisEngineService.executeStatement(deleteSql);
        }

        // 追加插入
        String insertSql = String.format("INSERT INTO %s SELECT * FROM %s", targetTable, sourceTable);
        log.info("执行投递 SQL: {}", insertSql);
        analysisEngineService.executeStatement(insertSql);

        log.info("数据表投递完成: exportId={}, targetTable={}", exportId, targetTable);
    }

    /**
     * 文件存储投递：MinIO/HDFS/OSS 等
     * <p>从数据源 config 获取 bucket，从 export_config 获取 objectPath，执行时替换模板变量。</p>
     */
    private void executeFileExport(String exportId, String groupId, String sourceTable,
                                   ExportConfig config, String datasourceType) {
        // 从数据源 config 提取 bucket
        DataSourceDTO ds = dataSourceService.getDetail(config.getDatasourceId());
        String bucket = extractFieldFromConfig(ds.getConfig(), "bucket");

        // 替换模板变量
        String objectPath = resolveTemplateVariables(config.getObjectPath(), groupId, exportId);

        log.info("开始执行文件存储投递: exportId={}, bucket={}, objectPath={}", exportId, bucket, objectPath);
        // TODO: 实现文件存储投递逻辑
        log.warn("文件存储投递功能尚未实现，当前为占位实现");
    }

    /**
     * 消息队列投递：Kafka/RabbitMQ/RocketMQ 等
     * <p>从数据源 config 获取 topic，投递 ID 作为消息 Key（Tag）。</p>
     */
    private void executeTopicExport(String exportId, String groupId, String sourceTable,
                                    ExportConfig config, String datasourceType) {
        // 从数据源 config 提取 topic
        DataSourceDTO ds = dataSourceService.getDetail(config.getDatasourceId());
        String topic = extractFieldFromConfig(ds.getConfig(), "topic");

        log.info("开始执行消息队列投递: exportId={}, topic={}, messageKey={}", exportId, topic, exportId);
        // TODO: 实现消息队列投递逻辑，投递 ID 作为消息 Key
        log.warn("消息队列投递功能尚未实现，当前为占位实现");
    }

    // =================================================================================================================
    // 配置解析辅助方法
    // =================================================================================================================

    /**
     * 解析投递配置 JSON
     */
    private ExportConfig parseExportConfig(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ExportConfig.class);
        } catch (Exception e) {
            log.error("解析投递配置失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 应用投递：从 Application.targetConfig 解析目标配置
     */
    private ExportConfig resolveApplicationTargetConfig(Export export) {
        ExportConfig config = parseExportConfig(export.getExportConfig());
        String applicationId = config != null ? config.getApplicationId() : null;
        if (StringUtils.isBlank(applicationId)) {
            throw new RuntimeException("应用投递未配置 applicationId: " + export.getExportId());
        }
        // applicationId 存储的是 appKey
        Application app = applicationService.getByAppKey(applicationId);
        if (app == null) {
            throw new RuntimeException("应用不存在: " + applicationId);
        }
        ExportConfig appConfig = parseExportConfig(app.getTargetConfig());
        if (appConfig == null) {
            throw new RuntimeException("应用投递目标配置解析失败: " + applicationId);
        }
        return appConfig;
    }

    /**
     * 根据数据源类型推断目标类型
     */
    private String resolveTargetType(String datasourceType, ExportConfig config) {
        if (StringUtils.isBlank(datasourceType)) {
            // 应用投递，暂默认 table
            return "table";
        }
        String type = datasourceType.toLowerCase();
        if (TABLE_TYPES.contains(type)) {
            return "table";
        }
        if (FILE_TYPES.contains(type)) {
            return "file";
        }
        if (TOPIC_TYPES.contains(type)) {
            return "topic";
        }
        if (INDEX_TYPES.contains(type)) {
            return "index";
        }
        throw new IllegalArgumentException("不支持的数据源类型: " + datasourceType);
    }

    /**
     * 从数据源 config JSON 中提取指定字段
     */
    private String extractFieldFromConfig(String configJson, String field) {
        if (StringUtils.isBlank(configJson)) {
            throw new RuntimeException("数据源配置为空");
        }
        try {
            Map<String, Object> configMap = gson.fromJson(configJson, Map.class);
            Object value = configMap.get(field);
            if (value == null || StringUtils.isBlank(value.toString())) {
                throw new RuntimeException("数据源配置中未包含 " + field + " 字段");
            }
            return value.toString();
        } catch (Exception e) {
            throw new RuntimeException("解析数据源配置失败: " + e.getMessage());
        }
    }

    /**
     * 替换模板变量：{groupId} → 实际群组ID，{timestamp} → 当前时间戳
     */
    private String resolveTemplateVariables(String template, String groupId, String exportId) {
        if (StringUtils.isBlank(template)) {
            return template;
        }
        String result = template;
        if (StringUtils.isNotBlank(groupId)) {
            result = result.replace("{groupId}", groupId);
        }
        result = result.replace("{timestamp}", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        result = result.replace("{exportId}", exportId);
        return result;
    }

    /**
     * 从 Export 的 export_config 中获取群组ID
     */
    private String getGroupIdFromConfig(Export export) {
        ExportConfig config = parseExportConfig(export.getExportConfig());
        if (config == null) {
            throw new RuntimeException("投递配置解析失败: " + export.getExportId());
        }
        // 尝试从 ExportConfig 的 groupId 字段获取
        String groupId = config.getGroupId();
        if (StringUtils.isBlank(groupId)) {
            // 兼容旧数据：直接从 JSON 中提取
            try {
                Map<String, Object> configMap = gson.fromJson(export.getExportConfig(), Map.class);
                Object gid = configMap.get("group_id");
                if (gid != null) {
                    groupId = gid.toString();
                }
            } catch (Exception e) {
                log.warn("从 export_config 提取 group_id 失败: {}", e.getMessage());
            }
        }
        if (StringUtils.isBlank(groupId)) {
            throw new RuntimeException("投递配置中未包含 group_id: " + export.getExportId());
        }
        return groupId;
    }
}
