package com.data.profile.web.engine;

import com.data.connector.api.ConnectorFactory;
import com.data.connector.api.TypeConverter;
import com.data.engine.api.AnalysisEngineFactory;
import com.data.engine.api.catalog.EngineCatalog;
import com.data.engine.api.query.EngineQuery;
import com.data.engine.api.schema.Column;
import com.data.engine.api.schema.SchemaDiff;
import com.data.engine.api.schema.TableSchema;
import com.data.engine.api.sink.EngineSink;
import com.data.profile.common.enums.DataType;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.Engine;
import com.data.profile.web.service.EngineService;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.data.profile.common.domain.Constant.ENGINE_DATASET_TABLE_PREFIX;

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
    private SqlTemplateEngine sqlTemplateEngine;

    // -------------------------------------------------------------------------
    // SQL 执行能力（群组圈选、预估等场景使用）
    // -------------------------------------------------------------------------

    /**
     * 执行 COUNT 查询并返回结果数。
     */
    public long executeCountQuery(String sql) throws Exception {
        return getEngineQuery().executeCount(sql);
    }

    /**
     * 执行 DDL/DML（建表、TRUNCATE、INSERT INTO ... SELECT 等）。
     */
    public void executeStatement(String sql) throws Exception {
        getEngineQuery().executeStatement(sql);
    }

    /**
     * 执行 SELECT 查询并返回结果列表。
     * 每行数据以 Map 形式返回，key 为列名，value 为列值。
     */
    public List<Map<String, Object>> executeQueryList(String sql) throws Exception {
        return getEngineQuery().executeQuery(sql);
    }

    /**
     * 执行 SELECT 查询并返回结果。
     * 单行数据以 Map 形式返回，key 为列名，value 为列值。
     */
    public Map<String, Object> executeQuery(String sql) throws Exception {
        return getEngineQuery().executeQuery(sql).get(0);
    }

    /**
     * 随机抽样返回指定表的行（画像展示随机用户等场景）。
     *
     * <p>方言隔离：抽样 SQL 由引擎模板渲染（sql-templates/{engineType}/random_sample.ftl），
     * 服务层零 rand()/random() 方言感知。</p>
     *
     * @param tableName 表名
     * @param limit     抽样行数
     * @return 抽样行列表（key 为列名）
     */
    public List<Map<String, Object>> getRandomRows(String tableName, int limit) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);
        params.put("limit", limit);
        String sql = sqlTemplateEngine.render("random_sample.ftl", params);
        return executeQueryList(sql);
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
        String tableName = ENGINE_DATASET_TABLE_PREFIX + datasetId;
        String database = getString(parseConfig(analysisEngine.getConfig()), "database");
        //
        String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(analysisEngine.getEngineType()));
        AnalysisEngineFactory factory = PluginLoader.getPluginLoader(AnalysisEngineFactory.class).getOrCreatePlugin(pluginName);
        EngineCatalog catalog = factory.getEngineCatalog();
        if (catalog == null) {
            log.error("分析引擎 [{}] 未实现 EngineCatalog，跳过删除引擎表", analysisEngine.getEngineType());
            return;
        }

        Map<String, Object> engineConfig = parseConfig(analysisEngine.getConfig());
        try {
            catalog.init(engineConfig);
            catalog.dropTable(database, tableName);
            log.info("成功删除引擎表: {}.{}", database, tableName);
        } catch (Exception e) {
            log.error("删除引擎表 [{}.{}] 失败：{}", database, tableName, e.getMessage());
            throw new RuntimeException("删除引擎表失败");
        }
    }

    // -------------------------------------------------------------------------
    // 引擎表管理能力（DDL 走 EngineCatalog，DML 走 EngineSink）
    // -------------------------------------------------------------------------

    /**
     * 创建引擎表
     * @param tableName 表名
     * @param comment   表注释（可空）
     * @param columns   列定义
     * @param orderBy   排序键列名
     */
    public void createTable(String tableName, String comment, List<Column> columns, List<String> orderBy) {
        TableSchema schema = TableSchema.builder()
                .database(getDatabase())
                .tableName(tableName)
                .comment(comment)
                .columns(columns)
                .orderBy(orderBy)
                .build();
        try {
            EngineCatalog engineCatalog = getEngineCatalog();
            engineCatalog.createTable(schema);
            log.info("引擎表 {} 创建成功", tableName);
        } catch (Exception e) {
            log.error("引擎表 {} 创建失败", tableName, e);
            throw new RuntimeException("引擎表创建失败: " + tableName, e);
        }
    }

    /**
     * 引擎表是否存在（通过 EngineCatalog 元数据反查）。
     */
    public boolean tableExists(String tableName) {
        try {
            return getEngineCatalog().tableExists(getDatabase(), tableName);
        } catch (Exception e) {
            log.error("引擎表存在性检查失败: table={}", tableName, e);
            throw new RuntimeException("引擎表存在性检查失败: " + tableName, e);
        }
    }

    /**
     * 原子交换两张引擎表（通过 EngineCatalog.atomicSwap，如群组结果表新旧无空窗切换）。
     *
     * <p>引擎不支持时抛异常（如非 ClickHouse Atomic 库），由调用方决定降级策略。</p>
     *
     * @param tableA 表 A
     * @param tableB 表 B
     */
    public void swapTables(String tableA, String tableB) {
        try {
            getEngineCatalog().atomicSwap(getDatabase(), tableA, tableB);
            log.info("引擎表原子交换成功: {} <-> {}", tableA, tableB);
        } catch (Exception e) {
            log.error("引擎表原子交换失败: {} <-> {}", tableA, tableB, e);
            throw new RuntimeException("引擎表原子交换失败: " + tableA + " <-> " + tableB, e);
        }
    }

    /**
     * 反查默认库下引擎表的列名列表（TableManager schema 反查，导出场景组装同步契约用）。
     */
    public List<String> getEngineTableColumnNames(String tableName) {
        try {
            Engine analysisEngine = getDefaultAnalysisEngine();
            String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(analysisEngine.getEngineType()));
            AnalysisEngineFactory factory = PluginLoader.getPluginLoader(AnalysisEngineFactory.class).getOrCreatePlugin(pluginName);
            EngineCatalog catalog = factory.getEngineCatalog();
            if (catalog == null) {
                throw new IllegalStateException("分析引擎 [" + analysisEngine.getEngineType() + "] 未实现 EngineCatalog");
            }
            catalog.init(parseConfig(analysisEngine.getConfig()));
            TableSchema schema = catalog.getTableSchema(getDatabase(), tableName);
            if (schema == null) {
                throw new IllegalStateException("引擎表不存在: " + tableName);
            }
            return schema.columnNames();
        } catch (Exception e) {
            log.error("引擎表列名反查失败: table={}", tableName, e);
            throw new RuntimeException("引擎表列名反查失败: " + tableName, e);
        }
    }

    /**
     * 删除指定引擎表（通过 EngineCatalog，非阻塞）。
     *
     * @param tableName 引擎表名
     */
    public void dropTable(String tableName) {
        try {
            getEngineCatalog().dropTable(getDatabase(), tableName);
            log.info("引擎表删除成功: {}", tableName);
        } catch (Exception e) {
            log.error("引擎表删除失败: table={}", tableName, e);
        }
    }

    /**
     * 将 CSV 输入流导入到指定引擎表：EngineCatalog 建表 + EngineSink 写数据。
     * <p>CSV 第一行为表头（跳过），后续行为数据。写入失败时清理已创建的表。</p>
     *
     * @param tableName 引擎表名（如 profile_label_xxx）
     * @param csvStream CSV 输入流
     * @param columns   列定义（按 CSV 列顺序）
     * @param orderBy   排序键列名
     * @return 写入的记录数
     */
    public int importCsvToTable(String tableName, InputStream csvStream, List<Column> columns, List<String> orderBy) {
        try {
            // 1. 建表（DDL）
            createTable(tableName, null, columns, orderBy);
            // 2. 写数据（DML）
            EngineSink engineSink = getEngineSink();
            int count = engineSink.importFromStream(tableName, csvStream, columns);
            log.info("CSV 导入引擎表完成: table={}, count={}", tableName, count);
            return count;
        } catch (Exception e) {
            log.error("CSV 导入引擎表 {} 失败", tableName, e);
            dropTable(tableName);
            throw new RuntimeException("CSV 导入引擎表失败: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // 引擎插件产物获取（新体系）
    // -------------------------------------------------------------------------

    /**
     * 获取引擎 Catalog（DDL / 元数据），含 init 与 null 校验。
     */
    private EngineCatalog getEngineCatalog() {
        Engine analysisEngine = getDefaultAnalysisEngine();
        String engineType = analysisEngine.getEngineType();
        String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(engineType));
        AnalysisEngineFactory factory = PluginLoader.getPluginLoader(AnalysisEngineFactory.class).getOrCreatePlugin(pluginName);
        EngineCatalog catalog = factory.getEngineCatalog();
        if (catalog == null) {
            log.error("分析引擎 [{}] 未实现 EngineCatalog", engineType);
            throw new RuntimeException("分析引擎 [" + engineType + "] 未实现 EngineCatalog，请联系管理员");
        }
        try {
            catalog.init(parseConfig(analysisEngine.getConfig()));
        } catch (Exception e) {
            log.error("分析引擎 [{}] 初始化 EngineCatalog 失败: {}", engineType, e.getMessage());
            throw new RuntimeException("分析引擎 [" + engineType + "] 初始化 EngineCatalog 失败，请联系管理员");
        }
        return catalog;
    }

    /**
     * 获取引擎 Sink（DML 数据写入），含 init 与 null 校验。
     */
    private EngineSink getEngineSink() {
        Engine analysisEngine = getDefaultAnalysisEngine();
        String engineType = analysisEngine.getEngineType();
        String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(engineType));
        AnalysisEngineFactory factory = PluginLoader.getPluginLoader(AnalysisEngineFactory.class).getOrCreatePlugin(pluginName);
        EngineSink sink = factory.getEngineSink();
        if (sink == null) {
            log.error("分析引擎 [{}] 未实现 EngineSink", engineType);
            throw new RuntimeException("分析引擎 [" + engineType + "] 未实现 EngineSink，请联系管理员");
        }
        try {
            sink.init(parseConfig(analysisEngine.getConfig()));
        } catch (Exception e) {
            log.error("分析引擎 [{}] 初始化 EngineSink 失败: {}", engineType, e.getMessage());
            throw new RuntimeException("分析引擎 [" + engineType + "] 初始化 EngineSink 失败，请联系管理员");
        }
        return sink;
    }

    /**
     * 获取引擎 Query（交互式 SQL 执行通道），含 init 与 null 校验。
     */
    private EngineQuery getEngineQuery() {
        Engine analysisEngine = getDefaultAnalysisEngine();
        String engineType = analysisEngine.getEngineType();
        String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(engineType));
        AnalysisEngineFactory factory = PluginLoader.getPluginLoader(AnalysisEngineFactory.class).getOrCreatePlugin(pluginName);
        EngineQuery query = factory.getEngineQuery();
        if (query == null) {
            log.error("分析引擎 [{}] 未实现 EngineQuery", engineType);
            throw new RuntimeException("分析引擎 [" + engineType + "] 未实现 EngineQuery，请联系管理员");
        }
        try {
            query.init(parseConfig(analysisEngine.getConfig()));
        } catch (Exception e) {
            log.error("分析引擎 [{}] 初始化 EngineQuery 失败: {}", engineType, e.getMessage());
            throw new RuntimeException("分析引擎 [" + engineType + "] 初始化 EngineQuery 失败，请联系管理员");
        }
        return query;
    }

    /**
     * 获取默认分析引擎的数据库名。
     */
    private String getDatabase() {
        return getString(parseConfig(getDefaultAnalysisEngine().getConfig()), "database");
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
        EngineCatalog catalog = factory.getEngineCatalog();
        if (catalog == null) {
            throw new IllegalStateException("分析引擎 [" + analysisEngine.getEngineType()
                    + "] 未实现 EngineCatalog，无法自动建表 / Schema 演进");
        }
        Map<String, Object> engineConfig = parseConfig(analysisEngine.getConfig());
        catalog.init(engineConfig);

        if (!catalog.tableExists(target.getDatabase(), target.getTableName())) {
            log.info("引擎表不存在，自动创建: {}", target.getTableName());
            catalog.createTable(target);
            return;
        }
        TableSchema current = catalog.getTableSchema(target.getDatabase(), target.getTableName());
        SchemaDiff diff = catalog.diff(current, target);
        if (diff.isEmpty()) {
            log.info("引擎表 schema 无变更: {}", target.getTableName());
            return;
        }
        log.info("引擎表 schema 演进: table={}, add={}, drop={}, modify={}",
                target.getTableName(),
                diff.getAddColumns().size(), diff.getDropColumns().size(), diff.getModifyColumns().size());
        catalog.alterTable(target, diff);
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