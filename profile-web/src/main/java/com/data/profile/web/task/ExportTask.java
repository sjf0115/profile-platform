package com.data.profile.web.task;

import com.data.profile.web.dao.ExportMapper;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.model.Application;
import com.data.profile.web.model.Export;
import com.data.profile.web.model.ExportConfig;
import com.data.profile.web.service.ApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

import static com.data.profile.common.domain.Constant.ENGINE_GROUP_TABLE_PREFIX;

/**
 * 功能：群组投递计算任务
 * <p>负责投递执行（配置解析、目标类型推断、多目标投递）。</p>
 * <p>CRUD 服务(ExportService) 与计算任务(ExportTask) 分离。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class ExportTask {

    @Resource
    private ExportMapper exportMapper;
    @Resource
    private ApplicationService applicationService;
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
        if (export.getExportMode() == 2) {
            // 应用投递：从 Application.targetConfig 获取目标配置
            targetConfig = resolveApplicationTargetConfig(export);
        } else {
            // 数据源投递：直接使用 export_config
            targetConfig = parseExportConfig(export.getExportConfig());
            if (targetConfig == null) {
                throw new RuntimeException("投递配置解析失败: " + exportId);
            }
        }

        // 3. 推断目标类型
        String targetType = resolveTargetType(targetConfig);
        log.info("投递目标类型: exportId={}, targetType={}", exportId, targetType);

        // 4. 构建源表
        String groupId = getGroupIdFromConfig(export);
        String sourceTable = ENGINE_GROUP_TABLE_PREFIX + groupId;

        // 5. 按目标类型分发执行
        switch (targetType) {
            case "table":
                executeTableExport(exportId, groupId, sourceTable, targetConfig);
                break;
            case "file":
                executeFileExport(exportId, groupId, sourceTable, targetConfig);
                break;
            case "topic":
                executeTopicExport(exportId, groupId, sourceTable, targetConfig);
                break;
            case "index":
                executeIndexExport(exportId, groupId, sourceTable, targetConfig);
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
     */
    private void executeTableExport(String exportId, String groupId, String sourceTable,
                                    ExportConfig config) throws Exception {
        String targetTable = config.getDatabase() + "." + config.getTableName();
        String writeMode = config.getWriteMode();

        log.info("开始执行数据表投递: exportId={}, targetTable={}", exportId, targetTable);

        if ("upsert".equalsIgnoreCase(writeMode)) {
            // 覆盖模式：先删除已存在数据，再插入
            String deleteSql = String.format(
                    "ALTER TABLE %s DELETE WHERE entity_id IN (SELECT entity_id FROM %s)",
                    targetTable, sourceTable);
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
     * 文件存储投递：MinIO/HDFS/OSS 等（占位实现）
     */
    private void executeFileExport(String exportId, String groupId, String sourceTable,
                                   ExportConfig config) {
        log.info("开始执行文件存储投递: exportId={}, bucket={}, objectPath={}, fileFormat={}",
                exportId, config.getBucket(), config.getObjectPath(), config.getFileFormat());
        // TODO: 实现文件存储投递逻辑
        log.warn("文件存储投递功能尚未实现，当前为占位实现");
    }

    /**
     * 消息队列投递：Kafka/RabbitMQ/RocketMQ 等（占位实现）
     */
    private void executeTopicExport(String exportId, String groupId, String sourceTable,
                                    ExportConfig config) {
        log.info("开始执行消息队列投递: exportId={}, topic={}", exportId, config.getTopic());
        // TODO: 实现消息队列投递逻辑
        log.warn("消息队列投递功能尚未实现，当前为占位实现");
    }

    /**
     * ES 索引投递（占位实现）
     */
    private void executeIndexExport(String exportId, String groupId, String sourceTable,
                                    ExportConfig config) {
        log.info("开始执行 ES 索引投递: exportId={}, indexName={}", exportId, config.getIndexName());
        // TODO: 实现 ES 索引投递逻辑
        log.warn("ES 索引投递功能尚未实现，当前为占位实现");
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
        Long appId = Long.parseLong(applicationId);
        Optional<Application> appOpt = applicationService.getDetail(appId);
        if (!appOpt.isPresent()) {
            throw new RuntimeException("应用不存在: " + applicationId);
        }
        ExportConfig appConfig = parseExportConfig(appOpt.get().getTargetConfig());
        if (appConfig == null) {
            throw new RuntimeException("应用投递目标配置解析失败: " + applicationId);
        }
        return appConfig;
    }

    /**
     * 根据配置推断目标类型
     */
    private String resolveTargetType(ExportConfig config) {
        if (StringUtils.isNotBlank(config.getTableName())) {
            return "table";
        }
        if (StringUtils.isNotBlank(config.getBucket())) {
            return "file";
        }
        if (StringUtils.isNotBlank(config.getTopic())) {
            return "topic";
        }
        if (StringUtils.isNotBlank(config.getIndexName())) {
            return "index";
        }
        return "table";
    }

    /**
     * 从 Export 中获取群组ID
     */
    private String getGroupIdFromConfig(Export export) {
        // TODO: Export 模型需要关联 groupId，当前暂返回 null
        return null;
    }
}
