package com.data.profile.web.service;

import com.data.engine.api.EngineExecutor;
import com.data.engine.api.EngineFactory;
import com.data.engine.common.ExecutorRequest;
import com.data.engine.plugin.bean.JobTask;
import com.data.engine.plugin.utils.SeaTunnelConfigUtil;
import com.data.profile.common.domain.Constant;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 功能：数据集同步服务
 * 描述：实现从多种数据源同步数据到 ClickHouse
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/12 15:00
 */
@Slf4j
@Service
public class DatasetSyncService {

    @Resource
    private DatasetService datasetService;

    @Resource
    private DataSourceService dataSourceService;

    @Resource
    private EngineService engineService;

    // 默认 ClickHouse 配置（写死）
    private static final String DEFAULT_CLICKHOUSE_HOST = "localhost";
    private static final int DEFAULT_CLICKHOUSE_PORT = 8123;
    private static final String DEFAULT_CLICKHOUSE_DATABASE = "profile";
    private static final String DEFAULT_CLICKHOUSE_USERNAME = "default";
    private static final String DEFAULT_CLICKHOUSE_PASSWORD = "";

    /**
     * ClickHouse 引擎名称
     */
    private static final String ENGINE_CLICKHOUSE = "clickhouse";

    /**
     * 提交数据集同步任务（使用 SeaTunnel 引擎）
     *
     * @param datasetId 数据集ID
     * @return 任务ID
     */
    public String submitSyncJob(String datasetId) {
        log.info("Submitting sync job for dataset: {}", datasetId);

        try {
            // 1. 查询数据集信息
            Optional<Dataset> datasetOpt = datasetService.getDetail(datasetId);
            if (!datasetOpt.isPresent()) {
                throw new RuntimeException("Dataset not found: " + datasetId);
            }
            Dataset dataset = datasetOpt.get();

            // 2. 查询数据源配置
            DataSource dataSource = dataSourceService.getDetail(dataset.getDatasourceId());
            if (dataSource == null) {
                throw new RuntimeException("DataSource not found: " + dataset.getDatasourceId());
            }

            // 3. 构建 SeaTunnel Job 配置
            String jobConfig = buildSeaTunnelConfig(dataset, dataSource);
            log.info("Generated SeaTunnel config: {}", jobConfig);

            // 4. 提交任务到引擎
            String jobId = engineService.submitJob(jobConfig);
            log.info("Sync job submitted, jobId: {}", jobId);

            return jobId;

        } catch (Exception e) {
            log.error("Failed to submit sync job for dataset: {}", datasetId, e);
            throw new RuntimeException("提交同步任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用 ClickHouse 引擎直接同步（小批量数据）
     *
     * @param datasetId 数据集ID
     * @return 任务ID
     */
    public String submitClickHouseSyncJob(String datasetId) {
        log.info("Submitting ClickHouse sync job for dataset: {}", datasetId);

        try {
            // 1. 查询数据集信息
            Optional<Dataset> datasetOpt = datasetService.getDetail(datasetId);
            if (!datasetOpt.isPresent()) {
                throw new RuntimeException("Dataset not found: " + datasetId);
            }
            Dataset dataset = datasetOpt.get();

            // 2. 构建执行请求
            ExecutorRequest request = buildClickHouseRequest(dataset);

            // 3. 获取 ClickHouse 引擎执行器
            EngineFactory engineFactory = PluginLoader.getPluginLoader(EngineFactory.class)
                    .getOrCreatePlugin(ENGINE_CLICKHOUSE);
            EngineExecutor executor = engineFactory.getExecutor();

            // 4. 初始化并执行任务
            String jobId = generateJobId();
            request.setJobId(jobId);
            executor.init(request, log, null);

            // 异步执行
            new Thread(() -> {
                try {
                    executor.execute();
                } catch (Exception e) {
                    log.error("ClickHouse sync job failed: {}", jobId, e);
                }
            }).start();

            log.info("ClickHouse sync job submitted, jobId: {}", jobId);
            return jobId;

        } catch (Exception e) {
            log.error("Failed to submit ClickHouse sync job for dataset: {}", datasetId, e);
            throw new RuntimeException("提交 ClickHouse 同步任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建 SeaTunnel 配置
     */
    private String buildSeaTunnelConfig(Dataset dataset, DataSource dataSource) throws IOException {
        // 1. 构建 Source 任务
        JobTask sourceTask = buildSourceTask(dataset, dataSource);

        // 2. 构建 Sink 任务（ClickHouse）
        JobTask sinkTask = buildSinkTask(dataset);

        // 3. 生成完整配置
        String sourceConfig = SeaTunnelConfigUtil.generateJobConfig(sourceTask);
        String sinkConfig = SeaTunnelConfigUtil.generateJobConfig(sinkTask);

        // 合并 Source 和 Sink 配置
        return mergeConfig(sourceConfig, sinkConfig);
    }

    /**
     * 构建 Source 任务
     */
    private JobTask buildSourceTask(Dataset dataset, DataSource dataSource) {
        // 解析数据源配置
        Map<String, Object> dsConfig = JSONUtils.parseObject(dataSource.getConfig(), Map.class);

        // 构建连接配置
        Map<String, Object> connectionConfig = new HashMap<>();
        connectionConfig.put("url", dsConfig.get("jdbcUrl"));
        connectionConfig.put("driver", dsConfig.get("driver"));
        connectionConfig.put("user", dsConfig.get("username"));
        connectionConfig.put("password", dsConfig.get("password"));

        // 构建查询 SQL
        String columns = buildColumns(dataset.getFields());
        String querySql = String.format("SELECT %s FROM %s", columns, dataset.getTableName());

        connectionConfig.put("query", querySql);

        // 确定 Connector 类型
        String connectorType = determineConnectorType(dataSource.getDatasourceType());

        return JobTask.builder()
                .type("source")
                .connectorType(connectorType)
                .name("dataset_source_" + dataset.getDatasetId())
                .config(JSONUtils.toJsonString(connectionConfig))
                .selectTableFields(buildSelectFields(dataset.getFields()))
                .outputSchema(buildOutputSchema(dataset))
                .dataSourceId(dataset.getId())
                .build();
    }

    /**
     * 构建 Sink 任务（ClickHouse）
     */
    private JobTask buildSinkTask(Dataset dataset) {
        Map<String, Object> sinkConfig = new HashMap<>();
        sinkConfig.put("host", DEFAULT_CLICKHOUSE_HOST + ":" + DEFAULT_CLICKHOUSE_PORT);
        sinkConfig.put("database", DEFAULT_CLICKHOUSE_DATABASE);
        sinkConfig.put("table", dataset.getDatasetId());
        sinkConfig.put("username", DEFAULT_CLICKHOUSE_USERNAME);
        sinkConfig.put("password", DEFAULT_CLICKHOUSE_PASSWORD);
        sinkConfig.put("bulk_size", 1000);

        return JobTask.builder()
                .type("sink")
                .connectorType("clickhouse")
                .name("dataset_sink_" + dataset.getDatasetId())
                .config(JSONUtils.toJsonString(sinkConfig))
                .build();
    }

    /**
     * 构建 ClickHouse 执行请求
     */
    private ExecutorRequest buildClickHouseRequest(Dataset dataset) {
        Map<String, Object> config = new HashMap<>();
        config.put("host", DEFAULT_CLICKHOUSE_HOST);
        config.put("port", DEFAULT_CLICKHOUSE_PORT);
        config.put("database", DEFAULT_CLICKHOUSE_DATABASE);
        config.put("username", DEFAULT_CLICKHOUSE_USERNAME);
        config.put("password", DEFAULT_CLICKHOUSE_PASSWORD);
        config.put("sourceTable", dataset.getTableName());
        config.put("targetTable", dataset.getDatasetId());
        config.put("columns", buildColumnList(dataset.getFields()));
        config.put("batchSize", 1000);

        return ExecutorRequest.builder()
                .config(config)
                .build();
    }

    /**
     * 构建字段列表
     */
    private String buildColumns(List<DatasetField> fields) {
        if (fields == null || fields.isEmpty()) {
            return "*";
        }
        return fields.stream()
                .map(DatasetField::getFieldName)
                .collect(Collectors.joining(", "));
    }

    /**
     * 构建字段列表（List）
     */
    private List<String> buildColumnList(List<DatasetField> fields) {
        if (fields == null || fields.isEmpty()) {
            return java.util.Collections.singletonList("*");
        }
        return fields.stream()
                .map(DatasetField::getFieldName)
                .collect(Collectors.toList());
    }

    /**
     * 构建 SelectFields JSON
     */
    private String buildSelectFields(List<DatasetField> fields) {
        Map<String, Object> result = new HashMap<>();
        if (fields == null || fields.isEmpty()) {
            result.put("tableFields", java.util.Collections.emptyList());
            result.put("all", true);
        } else {
            List<String> fieldNames = fields.stream()
                    .map(DatasetField::getFieldName)
                    .collect(Collectors.toList());
            result.put("tableFields", fieldNames);
            result.put("all", false);
        }
        return JSONUtils.toJsonString(result);
    }

    /**
     * 构建 Output Schema
     */
    private String buildOutputSchema(Dataset dataset) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("tableName", dataset.getTableName());
        schema.put("database", "default");

        if (dataset.getFields() != null) {
            List<Map<String, Object>> fieldSchemas = dataset.getFields().stream()
                    .map(this::convertFieldToSchema)
                    .collect(Collectors.toList());
            schema.put("fields", fieldSchemas);
        }

        return JSONUtils.toJsonString(java.util.Collections.singletonList(schema));
    }

    /**
     * 转换字段为 Schema
     */
    private Map<String, Object> convertFieldToSchema(DatasetField field) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("name", field.getFieldName());
        schema.put("type", field.getFieldType());
        schema.put("comment", field.getFieldDesc());
        schema.put("primaryKey", false);
        schema.put("nullable", true);
        schema.put("outputDataType", convertToSeaTunnelType(field.getFieldType()));
        return schema;
    }

    /**
     * 转换字段类型为 SeaTunnel 类型
     */
    private String convertToSeaTunnelType(String dbType) {
        if (StringUtils.isBlank(dbType)) {
            return "STRING";
        }
        String upperType = dbType.toUpperCase();
        switch (upperType) {
            case "INT":
            case "INTEGER":
                return "INT";
            case "BIGINT":
            case "LONG":
                return "BIGINT";
            case "SMALLINT":
            case "TINYINT":
                return "SMALLINT";
            case "FLOAT":
            case "REAL":
                return "FLOAT";
            case "DOUBLE":
                return "DOUBLE";
            case "DECIMAL":
            case "NUMERIC":
                return "DECIMAL";
            case "BOOLEAN":
            case "BIT":
                return "BOOLEAN";
            case "DATE":
                return "DATE";
            case "TIME":
                return "TIME";
            case "TIMESTAMP":
            case "DATETIME":
                return "TIMESTAMP";
            case "BINARY":
            case "VARBINARY":
            case "BLOB":
                return "BYTES";
            case "VARCHAR":
            case "CHAR":
            case "TEXT":
            case "STRING":
            default:
                return "STRING";
        }
    }

    /**
     * 确定 Connector 类型
     */
    private String determineConnectorType(String datasourceType) {
        if (StringUtils.isBlank(datasourceType)) {
            return "jdbc";
        }
        String type = datasourceType.toLowerCase();
        switch (type) {
            case "mysql":
            case "postgresql":
            case "oracle":
            case "sqlserver":
                return "jdbc";
            case "clickhouse":
                return "clickhouse";
            default:
                return "jdbc";
        }
    }

    /**
     * 合并 Source 和 Sink 配置
     */
    private String mergeConfig(String sourceConfig, String sinkConfig) {
        // 简单合并，实际应该解析并重新组装
        return String.format(
                "env {\n" +
                        "  job.mode = \"BATCH\"\n" +
                        "  parallelism = 2\n" +
                        "}\n" +
                        "%s\n" +
                        "transform {\n" +
                        "}\n" +
                        "%s",
                sourceConfig, sinkConfig
        );
    }

    /**
     * 生成任务ID
     */
    private String generateJobId() {
        return "JOB_" + System.currentTimeMillis();
    }
}
