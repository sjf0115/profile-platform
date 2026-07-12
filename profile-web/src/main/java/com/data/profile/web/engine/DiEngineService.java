package com.data.profile.web.engine;

import com.data.engine.api.DiEngineExecutor;
import com.data.engine.api.DiEngineFactory;
import com.data.engine.api.DiRequestBuilder;
import com.data.engine.api.SyncContext;
import com.data.engine.api.schema.TableSchema;
import com.data.engine.common.ExecutorRequest;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.config.ProfileEngineConfig;
import com.data.profile.web.converter.DataSourceConverter;
import com.data.profile.web.dto.DataSourceDTO;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.Engine;
import com.data.profile.web.service.DataSourceService;
import com.data.profile.web.service.DatasetFieldService;
import com.data.profile.web.service.DatasetService;
import com.data.profile.web.service.EngineService;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 功能：同步引擎服务
 * <p>负责：同步任务构建与提交。</p>
 *
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class DiEngineService {

    /** 集成引擎类别 */
    private static final String CATEGORY_DI = "di";
    /** 分析引擎类别 */
    private static final String CATEGORY_ANALYSIS = "analysis";

    @Resource
    private EngineService engineService;

    @Resource
    private ProfileEngineConfig engineProperties;

    @Resource
    private DatasetService datasetService;

    @Resource
    private DataSourceService dataSourceService;

    @Resource
    private AnalysisEngineService analysisEngineService;

    @Resource
    private DatasetFieldService datasetFieldService;

    /**
     * 同步数据集到分析引擎（一站式业务方法）。
     * <p>内部解析默认 DI 引擎和分析引擎，业务层无需感知 {@link Engine} 对象。</p>
     */
    public void syncDataset(Dataset dataset, DataSource dataSource, TableSchema target) throws Exception {
        Engine analysisEngine = getDefaultAnalysisEngine();
        submitSyncTask(dataset, dataSource, analysisEngine, target);
    }

    /**
     * 获取默认分析引擎，不存在则抛异常。
     */
    private Engine getDefaultAnalysisEngine() {
        Engine engine = engineService.getDefaultEngineByCategory(CATEGORY_ANALYSIS);
        if (engine == null) {
            throw new IllegalStateException("未找到可用的分析引擎(analysis)，请在引擎管理中设置默认分析引擎");
        }
        return engine;
    }

    /**
     * 执行数据集同步任务（完整流程）。
     * <p>包含：查数据集 → 查数据源 → 建表/演进 → 同步数据。</p>
     *
     * @param datasetId 数据集ID
     */
    public void executeDatasetSync(String datasetId) throws Exception {
        Optional<Dataset> opt = datasetService.getDetail(datasetId);
        if (!opt.isPresent()) {
            throw new IllegalStateException("数据集不存在: " + datasetId);
        }
        Dataset dataset = opt.get();

        DataSourceDTO dataSourceDTO = dataSourceService.getDetail(dataset.getDatasourceId());
        DataSource dataSource = DataSourceConverter.dto2do(dataSourceDTO);
        if (dataSource == null) {
            throw new IllegalStateException("数据源不存在: " + dataset.getDatasourceId());
        }

        // 1. 查询数据集字段
        List<DatasetField> fields = datasetFieldService.getListByDatasetId(datasetId);

        // 2. 构建目标 Schema + 建表/演进
        String tableName = "profile_dataset_" + datasetId;
        TableSchema tableSchema = analysisEngineService.buildAndUpsertTable(dataset, dataSource, tableName, fields);

        // 3. 同步数据
        syncDataset(dataset, dataSource, tableSchema);

        log.info("数据集同步完成: datasetId={}", datasetId);
    }

    // -----------------------------------------------------------------
    // 以下方法保留 Engine 参数，供需要指定引擎的高级场景使用
    // -----------------------------------------------------------------

    /**
     * 提交数据同步任务：业务库 → 分析引擎表。
     * <p>通过 {@link DiEngineFactory#getRequestBuilder()} 与 {@link DiEngineFactory#getExecutor()}
     * 协作完成"构建 + 执行"两阶段。</p>
     */
    public void submitSyncTask(Dataset dataset, DataSource dataSource, Engine analysisEngine, TableSchema target) throws Exception {
        List<String> columns = target.columnNames();
        if (columns.isEmpty()) {
            log.warn("数据集 {} 无可导入字段，跳过同步", dataset.getDatasetId());
            return;
        }

        // 1. 解析同步引擎
        Engine diEngine = engineService.getDefaultEngineByCategory(CATEGORY_DI);
        if (diEngine == null) {
            throw new IllegalStateException("未找到可用的集成引擎，请在引擎管理中设置默认集成引擎");
        }
        String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(diEngine.getEngineType()));
        DiEngineFactory factory = PluginLoader.getPluginLoader(DiEngineFactory.class).getOrCreatePlugin(pluginName);

        // 2. 构建通用 SyncContext
        String jobId = "di_" + dataset.getDatasetId() + "_" + System.currentTimeMillis();
        SyncContext context = SyncContext.builder()
                .jobId(jobId)
                .source(buildSourceEndpoint(dataSource, dataset.getTableName(), columns))
                .target(buildTargetEndpoint(analysisEngine, target.getTableName(), columns))
                .channel(engineProperties.getSyncChannel())
                .errorRecord(engineProperties.getSyncErrorRecord())
                .build();

        // 3. 通过引擎自带 RequestBuilder 转换为引擎私有 ExecutorRequest
        DiRequestBuilder builder = factory.getRequestBuilder();
        ExecutorRequest execReq = builder.buildRequest(context);

        // 4. 执行同步任务
        DiEngineExecutor executor = factory.getExecutor();
        executor.init(execReq, log, null);
        log.info("同步任务开始执行: jobId={}, datasetId={}, syncEngine={}",
                jobId, dataset.getDatasetId(), diEngine.getEngineType());
        executor.execute();

        // 5. 检查执行结果
        ProcessResult result = executor.getProcessResult();
        if (result == null || !result.isSuccess()) {
            String errMsg = result == null ? "无结果返回" : result.getErrorMsg();
            throw new RuntimeException("同步失败 [" + diEngine.getEngineType() + "]: " + errMsg);
        }
        log.info("同步任务完成: jobId={}, recordCount={}, duration={}ms",
                jobId, result.getRecordCount(), result.getDuration());
    }

    // -------------------------------------------------------------------------
    // 辅助方法
    // -------------------------------------------------------------------------

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
}
