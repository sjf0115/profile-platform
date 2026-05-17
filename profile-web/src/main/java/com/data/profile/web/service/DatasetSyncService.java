package com.data.profile.web.service;

import com.data.engine.api.AnalysisEngineExecutor;
import com.data.engine.api.AnalysisEngineFactory;
import com.data.engine.common.ExecutorRequest;
import com.data.profile.common.utils.JSONUtils;
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
 * 数据集同步服务
 * 负责协调引擎插件完成表管理和数据同步
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/14
 */
@Slf4j
@Service
public class DatasetSyncService {
    
    @Resource
    private DatasetService datasetService;
    
    @Resource
    private EngineService engineService;
    
    @Resource
    private DataSourceService dataSourceService;
    
    /**
     * 处理数据集（创建/修改后调用）
     * @param dataset 数据集
     */
    public void processDataset(Dataset dataset) {
        String datasetId = dataset.getDatasetId();
        String tableName = "profile_dataset_" + datasetId;
        
        try {
            // 1. 获取引擎配置
            Engine engine = engineService.getEngineOrDefault(dataset.getEngineId());
            // 2. 获取引擎插件执行器
            AnalysisEngineExecutor executor = getEngineExecutor(engine);
            
            // 3. 准备字段信息（只导入标记为导入的字段）
            List<Map<String, Object>> fields = buildFields(dataset.getFields());
            
            // 4. 创建/更新引擎表
            if (tableExists(engine, tableName)) {
                // 表已存在，判断是否需要修改
                log.info("引擎表已存在，执行修改操作: {}", tableName);
                executor.alterTable(tableName, fields, null, null);
            } else {
                // 表不存在，创建新表
                log.info("引擎表不存在，执行创建操作: {}", tableName);
                executor.createTable(
                        tableName, fields,
                        dataset.getEntityField(),
                        dataset.getPartitionField()
                );
            }
            
            // 5. 提交数据同步任务
            submitSyncTask(dataset, engine);
            
            log.info("数据集 {} 引擎处理完成", datasetId);
            
        } catch (Exception e) {
            log.error("数据集 {} 引擎处理失败", datasetId, e);
            throw new RuntimeException("数据集引擎处理失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取引擎执行器（通过 SPI 加载插件）
     */
    private AnalysisEngineExecutor getEngineExecutor(Engine engine) {
        String engineType = engine.getEngineType();
        
        // 通过 SPI 加载对应引擎插件
        AnalysisEngineFactory factory = PluginLoader.getPluginLoader(AnalysisEngineFactory.class)
                .getOrCreatePlugin(engineType);
        
        // 设置引擎配置
        Map<String, Object> configMap = JSONUtils.parseObject(engine.getConfig(), Map.class);
        Map<String, Object> executorConfig = new HashMap<>();
        executorConfig.put("engineConfig", configMap);
        
        // TODO: 初始化执行器
        // factory.getExecutor().init(request, log, null);
        
        return factory.getExecutor();
    }

    /**
     * 检查表是否存在
     */
    private boolean tableExists(Engine engine, String tableName) {
        // TODO: 通过引擎执行器检查表是否存在
        // 暂时返回 false，首次创建
        return false;
    }
    
    /**
     * 构建字段列表
     */
    private List<Map<String, Object>> buildFields(List<DatasetField> fields) {
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }
        
        return fields.stream()
            .filter(f -> f.getFieldStatus() != null && f.getFieldStatus() == 1) // 只导入标记为导入的字段
            .map(f -> {
                Map<String, Object> field = new HashMap<>();
                field.put("name", f.getFieldName());
                field.put("type", convertToEngineType(f.getFieldType()));
                field.put("comment", f.getFieldDesc());
                return field;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 提交数据同步任务
     */
    private void submitSyncTask(Dataset dataset, Engine engine) {
        // 构建同步配置
        Map<String, Object> syncConfig = new HashMap<>();
        syncConfig.put("datasetId", dataset.getDatasetId());
        syncConfig.put("tableName", "profile_dataset_" + dataset.getDatasetId());
        syncConfig.put("engineId", engine.getEngineId());
        syncConfig.put("engineType", engine.getEngineType());
        syncConfig.put("engineConfig", JSONUtils.parseObject(engine.getConfig(), Map.class));
        
        // 获取数据源配置
        try {
            DataSource dataSource = dataSourceService.getDetail(dataset.getDatasourceId());
            syncConfig.put("sourceConfig", JSONUtils.parseObject(dataSource.getConfig(), Map.class));
            syncConfig.put("sourceType", dataSource.getDatasourceType());
        } catch (Exception e) {
            log.error("获取数据源配置失败: {}", dataset.getDatasourceId(), e);
        }
        
        // TODO: 调用 SeaTunnel 引擎执行同步
        // pluginEngineService.submitJob(seaTunnelConfig);
        
        log.info("数据同步任务已提交: datasetId={}", dataset.getDatasetId());
    }
    
    /**
     * 字段类型转换：数据库类型 → ClickHouse 类型
     */
    private String convertToEngineType(String dbType) {
        if (StringUtils.isBlank(dbType)) return "String";
        
        switch (dbType.toUpperCase()) {
            case "BIGINT":
            case "LONG":
                return "Int64";
            case "INT":
            case "INTEGER":
                return "Int32";
            case "SMALLINT":
                return "Int16";
            case "TINYINT":
                return "Int8";
            case "FLOAT":
                return "Float32";
            case "DOUBLE":
                return "Float64";
            case "DECIMAL":
            case "NUMERIC":
                return "Decimal(18, 2)";
            case "VARCHAR":
            case "CHAR":
            case "TEXT":
            case "STRING":
                return "String";
            case "DATE":
                return "Date";
            case "DATETIME":
            case "TIMESTAMP":
                return "DateTime";
            case "BOOLEAN":
            case "BIT":
                return "UInt8";
            default:
                return "String";
        }
    }
}
