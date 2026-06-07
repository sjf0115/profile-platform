package com.data.profile.web.service;

import com.data.connector.api.ConnectorFactory;
import com.data.connector.api.TypeConverter;
import com.data.engine.api.DiEngineExecutor;
import com.data.engine.api.DiEngineFactory;
import com.data.engine.api.DiRequestBuilder;
import com.data.engine.api.SyncContext;
import com.data.engine.api.AnalysisEngineFactory;
import com.data.engine.api.schema.Column;
import com.data.engine.api.schema.SchemaDiff;
import com.data.engine.api.schema.TableManager;
import com.data.engine.api.schema.TableSchema;
import com.data.engine.common.ExecutorRequest;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.profile.common.enums.DataType;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.config.ProfileEngineConfig;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.Engine;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据集同步服务：协调"分析引擎"建表 + "集成引擎"同步。
 *
 * <p>架构原则：
 * <ul>
 *   <li>引擎全部 SPI 化：通过 {@link PluginLoader} 动态加载，无写死类型；</li>
 *   <li>单 SPI 多产物（Linkis EngineConnPlugin 模式）：
 *       {@link DiEngineFactory#getExecutor()} 与 {@link DiEngineFactory#getRequestBuilder()}
 *       同源同 category，杜绝漂移；</li>
 *   <li>引擎选择：取 Engine 表 is_default(同 category) 默认引擎。</li>
 * </ul>
 *
 * <p>Schema 推断与自动建表（P1）：
 * <ul>
 *   <li>Source 端：{@link ConnectorFactory#getTypeConverter()} 把数据源原生类型转为中性 {@link DataType}；</li>
 *   <li>Sink 端：{@link AnalysisEngineFactory#getTableManager()} 完成"建表 / 演进 / 反查"，
 *       内部完成中性 → 引擎私有类型映射；</li>
 *   <li>业务层只编排中性 {@link TableSchema}，与引擎、数据源彻底解耦。</li>
 * </ul>
 *
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class DatasetSyncService {

    /** 分析引擎类别（用于 Engine 表 engine_category 过滤） */
    private static final String CATEGORY_ANALYSIS = "analysis";
    /** 集成引擎类别 */
    private static final String CATEGORY_DI = "di";

    @Resource
    private EngineService engineService;

    @Resource
    private DataSourceService dataSourceService;

    @Resource
    private ProfileEngineConfig engineProperties;

    /**
     * 处理数据集（创建/修改后调用）：在分析引擎上创建同步目标表 + 提交同步任务。
     */
    public void processDataset(Dataset dataset) {
        String datasetId = dataset.getDatasetId();
        String tableName = "profile_dataset_" + datasetId;

        try {
            // 1. 解析分析引擎（取 analysis category 默认）
            Engine analysisEngine = resolveAnalysisEngine(dataset);

            // 2. 取数据源（用于 Source 侧字段类型转换）
            DataSource dataSource = dataSourceService.getDetail(dataset.getDatasourceId());
            if (dataSource == null) {
                throw new IllegalStateException("数据源不存在: " + dataset.getDatasourceId());
            }

            // 3. 通过 Source TypeConverter 把字段原生类型转中性 DataType，构造目标 TableSchema
            TableSchema target = buildTargetSchema(dataset, dataSource, tableName, analysisEngine);

            // 4. 通过 Sink TableManager 完成自动建表 / Schema 演进
            applySchema(analysisEngine, target);

            // 5. 提交同步任务
            submitSyncTask(dataset, dataSource, analysisEngine, target);

            log.info("数据集 {} 引擎处理完成", datasetId);
        } catch (Exception e) {
            log.error("数据集 {} 引擎处理失败", datasetId, e);
            throw new RuntimeException("数据集引擎处理失败: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // 引擎解析（三层兜底）
    // -------------------------------------------------------------------------

    /** 解析分析引擎：取引擎表 analysis category 默认引擎。 */
    private Engine resolveAnalysisEngine(Dataset dataset) {
        Engine engine = engineService.getDefaultEngineByCategory(CATEGORY_ANALYSIS);
        if (engine == null) {
            throw new IllegalStateException("未找到可用的分析引擎(analysis)，请在引擎管理中设置默认分析引擎");
        }
        return engine;
    }

    /** 解析同步引擎：取引擎表 di category 默认引擎。 */
    private Engine resolveSyncEngine(Dataset dataset) {
        Engine engine = engineService.getDefaultEngineByCategory(CATEGORY_DI);
        if (engine == null) {
            throw new IllegalStateException("未找到可用的集成引擎(di)，请在引擎管理中设置默认集成引擎");
        }
        return engine;
    }

    // -------------------------------------------------------------------------
    // Schema 推断 & 自动建表
    // -------------------------------------------------------------------------

    /**
     * 组装目标表中性 schema：
     * <ul>
     *   <li>列类型由 source TypeConverter 推断为中性 {@link DataType}；</li>
     *   <li>主键 + ORDER BY 均使用 dataset.entityField（单租户单引擎头画像场景下，
     *       这是点查与范围扫描的最优选择）；</li>
     *   <li>若 dataset.partitionField 存在，用于 PARTITION BY 且 ORDER BY 为 (partitionField, entityField)
     *       以同时快速命中分区裁剪与主体点查。</li>
     * </ul>
     */
    private TableSchema buildTargetSchema(Dataset dataset, DataSource dataSource,
                                          String tableName, Engine analysisEngine) {
        TypeConverter sourceTypeConverter = loadSourceTypeConverter(dataSource);
        String entityField = StringUtils.trimToNull(dataset.getEntityField());
        String partitionField = StringUtils.trimToNull(dataset.getPartitionField());

        List<DatasetField> importFields = filterImportFields(dataset.getFields());

        List<Column> columns = importFields.stream()
                .map(f -> {
                    boolean isEntity = entityField != null && entityField.equals(f.getFieldName());
                    return Column.builder()
                            .name(f.getFieldName())
                            .dataType(safeConvert(sourceTypeConverter, f.getFieldType()))
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
     * 解析 ORDER BY：仅使用 dataset 元数据。
     * <ul>
     *   <li>有 partitionField 且有 entityField → [partitionField, entityField]；</li>
     *   <li>仅有 entityField → [entityField]；</li>
     *   <li>都没有 → 空。</li>
     * </ul>
     */
    private List<String> resolveOrderBy(String entityField, String partitionField) {
        if (entityField == null) {
            return Collections.emptyList();
        }
        if (partitionField != null && !partitionField.equals(entityField)) {
            return Arrays.asList(partitionField, entityField);
        }
        return Collections.singletonList(entityField);
    }

    /**
     * 加载 source 端 TypeConverter：按 dataSource.datasourceType 路由 ConnectorFactory SPI。
     * 若无注册插件，返回宽松降级 converter（避免阻断主流程）。
     */
    private TypeConverter loadSourceTypeConverter(DataSource dataSource) {
        String category = StringUtils.lowerCase(StringUtils.trimToEmpty(dataSource.getDatasourceType()));
        if (category.isEmpty()) {
            log.warn("数据源 {} 未配置 datasourceType，字段类型将统一降级为 STRING_TYPE", dataSource.getDatasourceId());
            return fallbackTypeConverter();
        }
        try {
            ConnectorFactory factory = PluginLoader.getPluginLoader(ConnectorFactory.class)
                    .getOrCreatePlugin(category);
            TypeConverter tc = factory.getTypeConverter();
            return tc != null ? tc : fallbackTypeConverter();
        } catch (Throwable t) {
            log.warn("加载 ConnectorFactory[{}] 失败，字段类型将统一降级为 STRING_TYPE: {}", category, t.getMessage());
            return fallbackTypeConverter();
        }
    }

    /** 安全调用 TypeConverter：不识别类型时降级 STRING_TYPE。 */
    private DataType safeConvert(TypeConverter tc, String originType) {
        if (StringUtils.isBlank(originType)) {
            return DataType.STRING_TYPE;
        }
        try {
            DataType dt = tc.convert(originType);
            return dt != null ? dt : DataType.STRING_TYPE;
        } catch (Throwable t) {
            log.warn("字段类型 [{}] 在 source TypeConverter 不识别，降级 STRING_TYPE: {}", originType, t.getMessage());
            return DataType.STRING_TYPE;
        }
    }

    /** 兜底 TypeConverter：所有原生类型 → STRING_TYPE。 */
    private TypeConverter fallbackTypeConverter() {
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

    /**
     * 应用 schema：通过 SPI 获取分析引擎 TableManager，自动 create / diff / alter。
     */
    private void applySchema(Engine analysisEngine, TableSchema target) throws Exception {
        String pluginName = normalizePluginName(analysisEngine.getEngineType());
        AnalysisEngineFactory factory = PluginLoader.getPluginLoader(AnalysisEngineFactory.class)
                .getOrCreatePlugin(pluginName);
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
    // 集成引擎：同步
    // -------------------------------------------------------------------------

    /**
     * 提交数据同步任务：业务库 → 分析引擎表（如 ClickHouse）。
     *
     * <p>通过 {@link DiEngineFactory#getRequestBuilder()} 与 {@link DiEngineFactory#getExecutor()}
     * 协作完成"构建 + 执行"两阶段。</p>
     */
    private void submitSyncTask(Dataset dataset, DataSource dataSource,
                                Engine analysisEngine, TableSchema target) throws Exception {
        List<String> columns = target.columnNames();
        if (columns.isEmpty()) {
            log.warn("数据集 {} 无可导入字段，跳过同步", dataset.getDatasetId());
            return;
        }

        // 1. 解析同步引擎（di category）
        Engine syncEngine = resolveSyncEngine(dataset);

        // 2. SPI 加载同步引擎工厂（一次查询，拿到 builder + executor）
        DiEngineFactory factory = PluginLoader.getPluginLoader(DiEngineFactory.class)
                .getOrCreatePlugin(normalizePluginName(syncEngine.getEngineType()));

        // 3. 构建中性 SyncContext
        String jobId = "sync-" + dataset.getDatasetId() + "-" + System.currentTimeMillis();
        SyncContext context = SyncContext.builder()
                .jobId(jobId)
                .source(buildSourceEndpoint(dataSource, dataset.getTableName(), columns))
                .target(buildTargetEndpoint(analysisEngine, target.getTableName(), columns))
                .channel(engineProperties.getSyncChannel())
                .errorRecord(engineProperties.getSyncErrorRecord())
                .build();

        // 4. 通过引擎自带 RequestBuilder 转换为引擎私有 ExecutorRequest
        DiRequestBuilder builder = factory.getRequestBuilder();
        ExecutorRequest execReq = builder.buildRequest(context);

        // 5. 执行同步任务
        DiEngineExecutor executor = factory.getExecutor();
        executor.init(execReq, log, null);
        log.info("同步任务开始执行: jobId={}, datasetId={}, syncEngine={}",
                jobId, dataset.getDatasetId(), syncEngine.getEngineType());
        executor.execute();

        // 6. 检查执行结果
        ProcessResult result = executor.getProcessResult();
        if (result == null || !result.isSuccess()) {
            String errMsg = result == null ? "无结果返回" : result.getErrorMsg();
            throw new RuntimeException("同步失败 [" + syncEngine.getEngineType() + "]: " + errMsg);
        }
        log.info("同步任务完成: jobId={}, recordCount={}, duration={}ms",
                jobId, result.getRecordCount(), result.getDuration());
    }

    /** 从业务 DataSource 构建 SyncContext source 端点。 */
    private SyncContext.Endpoint buildSourceEndpoint(DataSource dataSource, String tableName, List<String> columns) {
        Map<String, Object> cfg = parseConfig(dataSource.getConfig());
        return SyncContext.Endpoint.builder()
                .category(dataSource.getDatasourceType())
                .host(getString(cfg, "host"))
                .port(getString(cfg, "port"))
                .database(firstNonBlank(getString(cfg, "database"), getString(cfg, "schema")))
                .username(firstNonBlank(getString(cfg, "user"), getString(cfg, "username")))
                .password(getString(cfg, "password"))
                .properties(getString(cfg, "properties"))
                .tableName(tableName)
                .columns(columns)
                .build();
    }

    /** 从分析 Engine 构建 SyncContext target 端点。 */
    private SyncContext.Endpoint buildTargetEndpoint(Engine analysisEngine, String tableName, List<String> columns) {
        Map<String, Object> cfg = parseConfig(analysisEngine.getConfig());
        return SyncContext.Endpoint.builder()
                .category(analysisEngine.getEngineType())
                .host(getString(cfg, "host"))
                .port(getString(cfg, "port"))
                .database(firstNonBlank(getString(cfg, "database"), getString(cfg, "schema")))
                .username(firstNonBlank(getString(cfg, "user"), getString(cfg, "username")))
                .password(getString(cfg, "password"))
                .properties(getString(cfg, "properties"))
                .tableName(tableName)
                .columns(columns)
                .writeMode(engineProperties.getSyncWriteMode())
                .batchSize(engineProperties.getSyncBatchSize())
                .build();
    }

    // -------------------------------------------------------------------------
    // 辅助方法
    // -------------------------------------------------------------------------

    /**
     * 将引擎/数据源类型规范化为 SPI 插件名：trim + 转小写。
     * <p>避免用户在前端填写 "ClickHouse" / "Clickhouse" 等大小写不一致时找不到插件。</p>
     */
    private String normalizePluginName(String engineType) {
        return StringUtils.lowerCase(StringUtils.trimToEmpty(engineType));
    }

    /** JSON 配置解析为 Map。 */
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

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (StringUtils.isNotBlank(v)) {
                return v;
            }
        }
        return null;
    }

    /** 仅保留 importStatus=1 的字段（用于同步列名）。 */
    private List<DatasetField> filterImportFields(List<DatasetField> fields) {
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }
        return fields.stream()
                .filter(f -> f.getImportStatus() != null && f.getImportStatus() == 1)
                .collect(Collectors.toList());
    }
}