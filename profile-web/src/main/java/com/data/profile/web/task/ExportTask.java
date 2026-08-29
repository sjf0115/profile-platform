package com.data.profile.web.task;

import com.data.connector.api.ConnectorFactory;
import com.data.connector.api.Executor;
import com.data.profile.common.domain.connector.request.ConnectorResponse;
import com.data.profile.common.domain.connector.request.ExecuteRequestParam;
import com.data.profile.common.enums.ExportMode;
import com.data.profile.web.dao.ExportMapper;
import com.data.profile.web.dto.DataSourceDTO;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.model.Application;
import com.data.profile.web.model.Export;
import com.data.profile.web.model.ExportConfig;
import com.data.profile.web.service.ApplicationService;
import com.data.profile.web.service.DataSourceService;
import com.data.profile.web.service.MinioService;
import com.data.spi.PluginLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


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

    /** 跨数据源投递批量写入单批行数 */
    private static final int EXPORT_BATCH_SIZE = 1000;

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
    private MinioService minioService;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 执行投递
     * @param exportId 投递ID
     */
    public void executeExport(String exportId) throws Exception {
        log.info("投递任务 [{}] 开始执行投递", exportId);

        // 1. 加载投递配置
        Export export = exportMapper.selectByExportId(exportId);
        if (export == null) {
            log.error("投递任务 [{}] 不存在", exportId);
            throw new RuntimeException("投递不存在: " + exportId);
        }

        // 2. 解析投递配置
        ExportConfig targetConfig = resolveExportConfig(export);
        String groupId = targetConfig.getGroupId();

        // 3. 推断目标类型
        DataSourceDTO ds = dataSourceService.getDetail(targetConfig.getDatasourceId());
        String datasourceType = ds.getDatasourceType();
        String targetType = resolveTargetType(datasourceType, targetConfig);
        log.info("投递目标类型: exportId={}, targetType={}", exportId, targetType);

        // 4. 按目标类型分发执行
        String sourceTable = ENGINE_GROUP_TABLE_PREFIX + groupId;
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
     * <p>目标是分析引擎同实例：走 EngineSink 服务端写入（零数据搬运）；
     * 其他数据源：待数据管道模式支持（EngineSource 流式读 → connector 写入）。</p>
     */
    private void executeTableExport(String exportId, String groupId, String sourceTable, ExportConfig config, String datasourceType) throws Exception {
        // 从数据源 config 提取 database
        DataSourceDTO ds = dataSourceService.getDetail(config.getDatasourceId());
        String database = extractFieldFromConfig(ds.getConfig(), "database");
        String targetTable = config.getTableName();
        String writeMode = config.getWriteMode();
        String targetColumn = config.getTargetColumn();

        log.info("开始执行数据表投递: exportId={}, target={}.{}, writeMode={}, targetColumn={}",
                exportId, database, targetTable, writeMode, targetColumn);

        if (analysisEngineService.isSameAsAnalysisEngine(ds)) {
            // 同实例投递：EngineSink 服务端写入
            analysisEngineService.transferToEngineTable(database, targetTable, sourceTable, writeMode, targetColumn);
        } else {
            // 跨数据源投递：EngineSource 流式读 → connector 批量写
            executeCrossDatasourceTableExport(exportId, ds, datasourceType, targetTable, sourceTable, writeMode, targetColumn);
        }
        log.info("数据表投递完成: exportId={}, target={}.{}", exportId, database, targetTable);
    }

    /**
     * 跨数据源表投递：EngineSource 流式读引擎表 → connector Executor 分批写入目标数据源。
     * <p>upsert 模式按批先删后写（分批删除的并集等价于全量先删后写）。</p>
     */
    private void executeCrossDatasourceTableExport(String exportId, DataSourceDTO ds, String datasourceType, String targetTable, String sourceTable, String writeMode, String targetColumn) throws Exception {
        // 1. 解析目标数据源类型的 Executor（写入能力）
        String dsType = StringUtils.lowerCase(StringUtils.trimToEmpty(datasourceType));
        ConnectorFactory factory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(dsType);
        Executor executor = factory.getExecutor();
        if (executor == null) {
            throw new RuntimeException("数据源类型 [" + datasourceType + "] 未实现投递写入能力（Executor）");
        }
        String dataSourceParam = ds.getConfig();

        // 2. 流式读源引擎表，按批写入目标数据源（避免全表驻留内存）
        final List<Map<String, Object>> batch = new ArrayList<>();
        final long[] total = {0};
        analysisEngineService.streamEngineTable(sourceTable, row -> {
            batch.add(row);
            if (batch.size() >= EXPORT_BATCH_SIZE) {
                flushExportBatch(executor, dataSourceParam, targetTable, batch, writeMode, targetColumn);
                total[0] += batch.size();
                batch.clear();
            }
        });
        if (!batch.isEmpty()) {
            flushExportBatch(executor, dataSourceParam, targetTable, batch, writeMode, targetColumn);
            total[0] += batch.size();
        }
        log.info("跨数据源表投递完成: exportId={}, target={}, rows={}", exportId, targetTable, total[0]);
    }

    /**
     * 写入单批数据：upsert 模式先按匹配列删除目标表已存在记录，再批量插入。
     */
    private void flushExportBatch(Executor executor, String dataSourceParam, String targetTable, List<Map<String, Object>> batch, String writeMode, String targetColumn) throws Exception {
        if ("upsert".equalsIgnoreCase(writeMode) && StringUtils.isNotBlank(targetColumn)) {
            ExecuteRequestParam deleteParam = new ExecuteRequestParam();
            deleteParam.setDataSourceParam(dataSourceParam);
            deleteParam.setScript(buildDeleteScript(targetTable, targetColumn, batch));
            executor.deleteData(deleteParam);
        }
        ExecuteRequestParam insertParam = new ExecuteRequestParam();
        insertParam.setDataSourceParam(dataSourceParam);
        insertParam.setTableName(targetTable);
        insertParam.setRows(batch);
        ConnectorResponse response = executor.insertData(insertParam);
        if (response != null && ConnectorResponse.Status.ERROR.equals(response.getStatus())) {
            throw new RuntimeException("跨数据源投递批量写入失败: " + response.getErrorMsg());
        }
    }

    /**
     * 构建 upsert 前置删除 SQL：删除目标表中匹配列值存在于本批数据的记录。
     */
    private String buildDeleteScript(String targetTable, String targetColumn, List<Map<String, Object>> batch) {
        String values = batch.stream()
                .map(row -> row.get(targetColumn))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .map(value -> "'" + escapeSqlString(value) + "'")
                .collect(Collectors.joining(", "));
        if (StringUtils.isBlank(values)) {
            // 本批匹配列值均为 null，无需删除（恒假条件避免 SQL 语法错误）
            return "DELETE FROM " + targetTable + " WHERE 1 = 0";
        }
        return "DELETE FROM " + targetTable + " WHERE `" + targetColumn + "` IN (" + values + ")";
    }

    /**
     * SQL 字符串值转义（单引号与反斜杠）。
     */
    private String escapeSqlString(String value) {
        return value.replace("\\", "\\\\").replace("'", "''");
    }

    /**
     * 文件存储投递：MinIO/HDFS/OSS 等（当前落地 MinIO，统一使用平台 MinIO bucket）。
     * <p>EngineSource 流式读 → CSV 临时文件 → 流式上传；objectPath 支持模板变量。</p>
     */
    private void executeFileExport(String exportId, String groupId, String sourceTable, ExportConfig config, String datasourceType) throws Exception {
        // 从数据源 config 提取 bucket（日志标识）
        DataSourceDTO ds = dataSourceService.getDetail(config.getDatasourceId());
        String bucket = extractFieldFromConfig(ds.getConfig(), "bucket");

        // 替换模板变量
        String objectPath = resolveTemplateVariables(config.getObjectPath(), groupId, exportId);

        log.info("开始执行文件存储投递: exportId={}, bucket={}, objectPath={}", exportId, bucket, objectPath);

        // 1. 流式读引擎表 → CSV 临时文件（单次遍历，避免全表驻留内存）
        Path tempFile = Files.createTempFile("export_" + exportId + "_", ".csv");
        final long[] total = {0};
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                analysisEngineService.streamEngineTable(sourceTable, row -> {
                    if (total[0] == 0) {
                        // 首行写 header（按首行列顺序）
                        writer.write(row.keySet().stream()
                                .map(this::escapeCsvField)
                                .collect(Collectors.joining(",")));
                        writer.newLine();
                    }
                    writer.write(row.values().stream()
                            .map(v -> escapeCsvField(v == null ? "" : String.valueOf(v)))
                            .collect(Collectors.joining(",")));
                    writer.newLine();
                    total[0]++;
                });
            }
            // 2. 流式上传到 MinIO（平台统一 bucket）
            try (InputStream is = Files.newInputStream(tempFile)) {
                minioService.uploadStream(is, objectPath, "text/csv");
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
        log.info("文件存储投递完成: exportId={}, objectPath={}, rows={}", exportId, objectPath, total[0]);
    }

    /**
     * CSV 字段转义：含逗号/双引号/换行时用双引号包裹，内部双引号翻倍。
     */
    private String escapeCsvField(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * 消息队列投递：Kafka/RabbitMQ/RocketMQ 等（当前落地 Kafka）。
     * <p>EngineSource 流式读 → 每行转 JSON 消息 → connector Executor 分批发送。</p>
     */
    private void executeTopicExport(String exportId, String groupId, String sourceTable, ExportConfig config, String datasourceType) throws Exception {
        // 从数据源 config 提取 topic
        DataSourceDTO ds = dataSourceService.getDetail(config.getDatasourceId());
        String topic = extractFieldFromConfig(ds.getConfig(), "topic");

        log.info("开始执行消息队列投递: exportId={}, topic={}, messageKey={}", exportId, topic, exportId);

        // 1. 解析目标数据源类型的 Executor（消息发送能力）
        String dsType = StringUtils.lowerCase(StringUtils.trimToEmpty(datasourceType));
        ConnectorFactory factory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(dsType);
        Executor executor = factory.getExecutor();
        if (executor == null) {
            throw new RuntimeException("数据源类型 [" + datasourceType + "] 未实现消息发送能力（Executor）");
        }

        // 2. 流式读源引擎表，按批发送（每行一条 JSON 消息）
        final List<Map<String, Object>> batch = new ArrayList<>();
        final long[] total = {0};
        analysisEngineService.streamEngineTable(sourceTable, row -> {
            batch.add(row);
            if (batch.size() >= EXPORT_BATCH_SIZE) {
                flushTopicBatch(executor, ds.getConfig(), topic, batch);
                total[0] += batch.size();
                batch.clear();
            }
        });
        if (!batch.isEmpty()) {
            flushTopicBatch(executor, ds.getConfig(), topic, batch);
            total[0] += batch.size();
        }
        log.info("消息队列投递完成: exportId={}, topic={}, rows={}", exportId, topic, total[0]);
    }

    /**
     * 发送单批消息：tableName 字段在消息队列场景承载 topic。
     */
    private void flushTopicBatch(Executor executor, String dataSourceParam, String topic, List<Map<String, Object>> batch) throws Exception {
        ExecuteRequestParam param = new ExecuteRequestParam();
        param.setDataSourceParam(dataSourceParam);
        param.setTableName(topic);
        param.setRows(batch);
        ConnectorResponse response = executor.insertData(param);
        if (response != null && ConnectorResponse.Status.ERROR.equals(response.getStatus())) {
            throw new RuntimeException("消息队列投递批量发送失败: " + response.getErrorMsg());
        }
    }

    // =================================================================================================================
    // 配置解析辅助方法
    // =================================================================================================================

    /**
     * 获取投递配置
     * @param export 投递信息
     */
    private ExportConfig resolveExportConfig(Export export) {
        ExportConfig exportConfig = gson.fromJson(export.getExportConfig(), ExportConfig.class);
        if (exportConfig == null) {
            log.error("获取 [{}] 投递任务投递配置失败", export.getExportId());
            throw new RuntimeException("投递配置获取失败");
        }

        Integer exportMode = export.getExportMode();
        if (Objects.equals(exportMode, ExportMode.APPLICATION.getCode())) {
            // 应用投递 从应用配置中获取投递配置
            String applicationId = exportConfig.getApplicationId();
            Application app = applicationService.getByAppKey(applicationId);
            if (app == null) {
                log.error("应用投递 [{}] 未找到到对应应用", applicationId);
                throw new RuntimeException("应用不存在");
            }
            ExportConfig appConfig = gson.fromJson(app.getTargetConfig(), ExportConfig.class);
            if (appConfig == null) {
                log.error("获取 [{}] 应用投递配置失败", applicationId);
                throw new RuntimeException("投递配置获取失败");
            }
            return appConfig;
        } else {
            // 数据源投递直接返回
            return exportConfig;
        }
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
}
