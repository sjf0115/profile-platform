package com.data.profile.web.task;

import com.data.engine.api.DiContext;
import com.data.engine.api.schema.TableSchema;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.profile.web.converter.DataSourceConverter;
import com.data.profile.web.converter.DatasetConverter;
import com.data.profile.web.dto.DataSourceDTO;
import com.data.profile.web.dto.DatasetDTO;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.engine.DiEngineService;
import com.data.profile.web.engine.DiEndpointResolver;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.service.DataSourceService;
import com.data.profile.web.service.DatasetFieldService;
import com.data.profile.web.service.DatasetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.data.profile.common.domain.Constant.ENGINE_DATASET_TABLE_PREFIX;

/**
 * 功能：数据集同步任务
 *
 * <p>职责：数据集同步的完整业务编排——查实体 → 建表/演进 → 同步数据。
 * 引擎提交能力由 {@link DiEngineService}（引擎门面）提供，
 * 建表/演进由 {@link AnalysisEngineService} 提供，本层不感知引擎细节。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class DatasetTask {
    @Resource
    private DiEngineService diEngineService;
    @Resource
    private AnalysisEngineService analysisEngineService;
    @Resource
    private DiEndpointResolver diEndpointResolver;
    @Resource
    private DatasetService datasetService;
    @Resource
    private DataSourceService dataSourceService;
    @Resource
    private DatasetFieldService datasetFieldService;

    /**
     * 执行数据集同步（完整流程）
     * <p>包含：查数据集 → 查数据源 → 建表/演进 → 同步数据。</p>
     * @param datasetId 数据集ID
     */
    public void execute(String datasetId) throws Exception {
        log.info("开始执行数据集 [{}] 同步", datasetId);
        DatasetDTO datasetDTO = datasetService.getDetail(datasetId);
        Dataset dataset = DatasetConverter.dto2do(datasetDTO);
        DataSourceDTO dataSourceDTO = dataSourceService.getDetail(datasetDTO.getDatasourceId());
        if (dataSourceDTO == null) {
            log.warn("数据源不存在");
            throw new IllegalStateException("数据源不存在: " + datasetDTO.getDatasourceId());
        }
        DataSource dataSource = DataSourceConverter.dto2do(dataSourceDTO);

        // 1. 构建目标 Schema 用于创建分析引擎目标表
        List<DatasetField> fields = datasetFieldService.getListByDatasetId(datasetId);
        String tableName = ENGINE_DATASET_TABLE_PREFIX + datasetId;
        TableSchema tableSchema = analysisEngineService.buildAndUpsertTable(dataset, dataSource, tableName, fields);

        // 2. 组装同步上下文用于提交任务到同步引擎同步数据（中性契约）
        List<String> columns = tableSchema.columnNames();
        if (columns.isEmpty()) {
            log.warn("数据集 [{}] 无可同步字段", datasetId);
            throw new IllegalStateException("数据集无可同步字段");
        }

        // Source: 三方数据源配置 + 来源表 + 来源列（connector 插件翻译）
        // Target: 分析引擎配置 + 目标表 + 目标列（引擎门面解析 + connector 插件翻译）
        DiContext context = DiContext.builder()
                .jobId("di_" + datasetId + "_" + System.currentTimeMillis())
                .source(diEndpointResolver.resolveSource(dataSource, dataset.getTableName(), columns))
                .target(diEngineService.resolveAnalysisTarget(tableSchema.getTableName(), columns))
                .build();
        ProcessResult result = diEngineService.sync(context);

        log.info("数据集同步完成: datasetId={}, recordCount={}, duration={}ms", datasetId, result.getRecordCount(), result.getDuration());
    }
}
