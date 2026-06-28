package com.data.profile.web.engine;

import com.data.connector.api.ConnectorFactory;
import com.data.connector.api.TypeConverter;
import com.data.engine.api.AnalysisEngineFactory;
import com.data.engine.api.schema.Column;
import com.data.engine.api.schema.SchemaDiff;
import com.data.engine.api.schema.TableManager;
import com.data.engine.api.schema.TableSchema;
import com.data.profile.common.enums.DataType;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.Engine;
import com.data.profile.web.service.DataSourceService;
import com.data.profile.web.service.EngineService;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能：分析引擎服务
 * <p>负责：Schema 推断、自动建表、Schema 演进。</p>
 *
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class AnalysisEngineService {

    /** 分析引擎类别 */
    private static final String CATEGORY_ANALYSIS = "analysis";

    @Resource
    private EngineService engineService;

    @Resource
    private DataSourceService dataSourceService;

    // -------------------------------------------------------------------------
    // SQL 执行能力（群组圈选、预估等场景使用）
    // -------------------------------------------------------------------------

    /**
     * 执行 COUNT 查询并返回结果数。
     */
    public long executeCountQuery(String sql) throws Exception {
        Engine analysisEngine = getDefaultAnalysisEngine();
        Map<String, Object> config = parseConfig(analysisEngine.getConfig());
        try (Connection conn = getAnalysisConnection(config);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        }
    }

    /**
     * 执行 DDL/DML（建表、TRUNCATE、INSERT INTO ... SELECT 等）。
     */
    public void executeStatement(String sql) throws Exception {
        Engine analysisEngine = getDefaultAnalysisEngine();
        Map<String, Object> config = parseConfig(analysisEngine.getConfig());
        try (Connection conn = getAnalysisConnection(config);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * 通过分析引擎配置建立 JDBC 连接。
     */
    private Connection getAnalysisConnection(Map<String, Object> config) throws Exception {
        String host = getString(config, "host");
        Object portObj = config.get("port");
        int port = portObj instanceof Number ? ((Number) portObj).intValue() : Integer.parseInt(String.valueOf(portObj));
        String database = getString(config, "database");
        String username = getString(config, "username");
        String password = getString(config, "password");
        String url = String.format("jdbc:clickhouse://%s:%d/%s", host, port, database);
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 构建目标 Schema 并在分析引擎上建表/演进（一站式业务方法）。
     * <p>内部解析默认分析引擎，业务层无需感知 {@link Engine} 对象。</p>
     *
     * @return 构建完成的 TableSchema
     */
    public TableSchema buildAndUpsertTable(Dataset dataset, DataSource dataSource, String tableName, List<DatasetField> fields) throws Exception {
        Engine analysisEngine = getDefaultAnalysisEngine();
        TableSchema tableSchema = buildTargetSchema(dataset, dataSource, tableName, analysisEngine, fields);
        upsertAnalysisEngineTable(analysisEngine, tableSchema);
        return tableSchema;
    }

    /**
     * 删除数据集对应的分析引擎表
     */
    public void dropDatasetTable(String datasetId) {
        // 获取执行引擎
        Engine analysisEngine = getDefaultAnalysisEngine();
        // 获取需要删除的数据库和数据表
        String tableName = "profile_dataset_" + datasetId;
        String database = getString(parseConfig(analysisEngine.getConfig()), "database");
        //
        String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(analysisEngine.getEngineType()));
        AnalysisEngineFactory factory = PluginLoader.getPluginLoader(AnalysisEngineFactory.class).getOrCreatePlugin(pluginName);
        TableManager tm = factory.getTableManager();
        if (tm == null) {
            log.error("分析引擎 [{}] 未实现 TableManager，跳过删除引擎表", analysisEngine.getEngineType());
            return;
        }

        Map<String, Object> engineConfig = parseConfig(analysisEngine.getConfig());
        try {
            tm.init(engineConfig);
            tm.dropTable(database, tableName);
            log.info("成功删除引擎表: {}.{}", database, tableName);
        } catch (Exception e) {
            log.error("删除引擎表 [{}.{}] 失败：{}", database, tableName, e.getMessage());
            throw new RuntimeException("删除引擎表失败");
        }
    }

    /**
     * 获取分析引擎
     */
    private Engine getDefaultAnalysisEngine() {
        Engine engine = engineService.getDefaultEngineByCategory(CATEGORY_ANALYSIS);
        if (engine == null) {
            throw new IllegalStateException("未找到可用的分析引擎(analysis)，请在引擎管理中设置默认分析引擎");
        }
        return engine;
    }

    // -----------------------------------------------------------------
    // 以下方法保留 Engine 参数，供需要指定引擎的高级场景使用
    // -----------------------------------------------------------------

    /**
     * 组装目标表通用 schema：
     * <ul>
     *   <li>列类型由 source TypeConverter 推断为中性 {@link DataType}；</li>
     *   <li>主键 + ORDER BY 均使用 dataset.entityField；</li>
     *   <li>若 dataset.partitionField 存在，用于 PARTITION BY。</li>
     * </ul>
     */
    public TableSchema buildTargetSchema(Dataset dataset, DataSource dataSource, String tableName, Engine analysisEngine, List<DatasetField> datasetFields) {
        TypeConverter sourceTypeConverter = loadSourceTypeConverter(dataSource);
        String entityField = StringUtils.trimToNull(dataset.getEntityField());
        String partitionField = StringUtils.trimToNull(dataset.getPartitionField());

        List<DatasetField> importFields = datasetFields.stream()
                .filter(f -> f.getImportStatus() != null && f.getImportStatus() == 1)
                .collect(Collectors.toList());

        List<Column> columns = importFields.stream()
                .map(f -> {
                    boolean isEntity = entityField != null && entityField.equals(f.getFieldName());
                    return Column.builder()
                            .name(f.getFieldName())
                            .dataType(dataTypeConvert(sourceTypeConverter, f.getFieldType()))
                            .comment(f.getFieldDesc())
                            .nullable(!isEntity)
                            .primaryKey(isEntity)
                            .build();
                })
                .collect(Collectors.toList());

        List<String> orderBy = resolveOrderBy(entityField, partitionField);

        return TableSchema.builder()
                .database(getString(parseConfig(analysisEngine.getConfig()), "database"))
                .tableName(tableName)
                .columns(columns)
                .orderBy(orderBy)
                .partitionBy(partitionField)
                .build();
    }

    /**
     * 应用 schema：通过 SPI 获取分析引擎 TableManager，自动 create / diff / alter。
     */
    public void upsertAnalysisEngineTable(Engine analysisEngine, TableSchema target) throws Exception {
        String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(analysisEngine.getEngineType()));
        AnalysisEngineFactory factory = PluginLoader.getPluginLoader(AnalysisEngineFactory.class).getOrCreatePlugin(pluginName);
        TableManager tm = factory.getTableManager();
        if (tm == null) {
            throw new IllegalStateException("分析引擎 [" + analysisEngine.getEngineType()
                    + "] 未实现 TableManager，无法自动建表 / Schema 演进");
        }
        Map<String, Object> engineConfig = parseConfig(analysisEngine.getConfig());
        tm.init(engineConfig);

        if (!tm.tableExists(target.getDatabase(), target.getTableName())) {
            log.info("引擎表不存在，自动创建: {}", target.getTableName());
            tm.createTable(target);
            return;
        }
        TableSchema current = tm.getTableSchema(target.getDatabase(), target.getTableName());
        SchemaDiff diff = tm.diff(current, target);
        if (diff.isEmpty()) {
            log.info("引擎表 schema 无变更: {}", target.getTableName());
            return;
        }
        log.info("引擎表 schema 演进: table={}, add={}, drop={}, modify={}",
                target.getTableName(),
                diff.getAddColumns().size(), diff.getDropColumns().size(), diff.getModifyColumns().size());
        tm.alterTable(target, diff);
    }

    // -------------------------------------------------------------------------
    // 辅助方法
    // -------------------------------------------------------------------------

    private List<String> resolveOrderBy(String entityField, String partitionField) {
        if (entityField == null) {
            return Collections.emptyList();
        }
        if (partitionField != null && !partitionField.equals(entityField)) {
            return Arrays.asList(partitionField, entityField);
        }
        return Collections.singletonList(entityField);
    }

    private TypeConverter loadSourceTypeConverter(DataSource dataSource) {
        String datasourceType = StringUtils.lowerCase(StringUtils.trimToEmpty(dataSource.getDatasourceType()));
        if (datasourceType.isEmpty()) {
            log.warn("数据源 {} 未配置 datasourceType，字段类型将统一降级为 STRING_TYPE", dataSource.getDatasourceId());
            return defaultTypeConverter();
        }
        try {
            ConnectorFactory factory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(datasourceType);
            TypeConverter typeConverter = factory.getTypeConverter();
            if (typeConverter != null) {
                return typeConverter;
            } else {
                return defaultTypeConverter();
            }
        } catch (Throwable t) {
            log.warn("加载 ConnectorFactory[{}] 失败，字段类型将统一降级为 STRING_TYPE: {}", datasourceType, t.getMessage());
            return defaultTypeConverter();
        }
    }

    private TypeConverter defaultTypeConverter() {
        return new TypeConverter() {
            @Override
            public DataType convert(String originType) {
                return DataType.STRING_TYPE;
            }

            @Override
            public String convertToOriginType(DataType dataType) {
                return "STRING";
            }
        };
    }

    private DataType dataTypeConvert(TypeConverter typeConverter, String originType) {
        if (StringUtils.isBlank(originType)) {
            return DataType.STRING_TYPE;
        }
        try {
            DataType dataType = typeConverter.convert(originType);
            return dataType != null ? dataType : DataType.STRING_TYPE;
        } catch (Throwable t) {
            log.warn("字段类型 [{}] 在 source TypeConverter 不识别，降级 STRING_TYPE: {}", originType, t.getMessage());
            return DataType.STRING_TYPE;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfig(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyMap();
        }
        return JSONUtils.parseObject(json, Map.class);
    }

    private String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }
}