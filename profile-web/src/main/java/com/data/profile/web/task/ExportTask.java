package com.data.profile.web.task;

import com.data.engine.api.DiContext;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.profile.common.enums.ExportMode;
import com.data.profile.web.dto.DataSourceDTO;
import com.data.profile.web.dto.ExportDTO;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.engine.DiEngineService;
import com.data.profile.web.engine.SyncEndpointResolver;
import com.data.profile.web.model.Application;
import com.data.profile.web.model.ExportConfig;
import com.data.profile.web.service.ApplicationService;
import com.data.profile.web.service.DataSourceService;
import com.data.profile.web.service.ExportService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.data.profile.common.domain.Constant.ENGINE_GROUP_TABLE_PREFIX;

/**
 * 功能：群组投递计算任务
 * <p>投递即同步：分析引擎数据表导出到任意三方去向均为数据搬运，
 * 统一组装中性 {@link DiContext} 提交给默认 DI 引擎（DataX/SeaTunnel），与 DatasetTask 完全同构。</p>
 * <p>投递配置即中性配置：用户按 connector 投递表单填写的参数原样透传（业界模式：DataWorks/Airbyte 参数透传），
 * 本层只做配置解析与模板渲染，不感知目标类型、不解析投递字段含义——语义由引擎插件按方言消费。</p>
 * <p>CRUD 服务(ExportService) 与计算任务(ExportTask) 分离。</p>
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

    @Resource
    private ExportService exportService;
    @Resource
    private ApplicationService applicationService;
    @Resource
    private DataSourceService dataSourceService;
    @Resource
    private AnalysisEngineService analysisEngineService;
    @Resource
    private DiEngineService diEngineService;
    @Resource
    private SyncEndpointResolver syncEndpointResolver;

    /**
     * 执行投递（提交同步任务）
     * @param exportId 投递ID
     */
    public void executeExport(String exportId) throws Exception {
        log.info("投递任务 [{}] 开始执行投递", exportId);

        // 1. 加载投递配置
        ExportDTO export = exportService.getDetail(exportId).orElse(null);
        if (export == null) {
            log.error("投递任务 [{}] 不存在", exportId);
            throw new RuntimeException("投递不存在: " + exportId);
        }

        // 2. 解析投递配置（应用投递 / 数据源投递）
        ExportConfig targetConfig = resolveExportConfig(export);
        String groupId = targetConfig.getGroupId();
        DataSourceDTO ds = dataSourceService.getDetail(targetConfig.getDatasourceId());

        // 3. 组装同步契约：source=分析引擎表，target=连接配置翻译 + 投递参数原样透传
        String sourceTable = ENGINE_GROUP_TABLE_PREFIX + groupId;
        List<String> columns = analysisEngineService.getEngineTableColumnNames(sourceTable);
        DiContext.Endpoint source = diEngineService.resolveAnalysisSource(sourceTable, columns);
        Map<String, Object> delivery = renderDeliveryConfig(targetConfig, exportId, groupId);
        DiContext.Endpoint target = syncEndpointResolver.resolveExportTarget(ds.getDatasourceType(), ds.getConfig(), delivery, columns);

        // 4. 提交 DI 引擎（提交 + 轮询至终态），与 DatasetTask 唯一出口一致
        DiContext context = DiContext.builder()
                .jobId("export_" + exportId + "_" + System.currentTimeMillis())
                .source(source)
                .target(target)
                .build();
        ProcessResult result = diEngineService.sync(context);
        log.info("投递执行完成: exportId={}, recordCount={}, duration={}ms",
                exportId, result.getRecordCount(), result.getDuration());
    }

    // =================================================================================================================
    // 投递配置填充（中性配置：模板渲染后原样透传，平台不解析字段含义）
    // =================================================================================================================

    /**
     * 投递配置渲染为中性参数：仅替换模板变量（平台调度语义），其余字段原样透传给引擎插件消费。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> renderDeliveryConfig(ExportConfig config, String exportId, String groupId) {
        ExportConfig rendered = config;
        if (StringUtils.isNotBlank(config.getObjectPath())) {
            rendered = gson.fromJson(gson.toJson(config), ExportConfig.class);
            rendered.setObjectPath(resolveTemplateVariables(config.getObjectPath(), groupId, exportId));
        }
        return gson.fromJson(gson.toJson(rendered), Map.class);
    }

    /**
     * 替换模板变量：{groupId} → 实际群组ID，{timestamp} → 当前时间戳，{exportId} → 投递ID
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

    // =================================================================================================================
    // 配置解析辅助方法
    // =================================================================================================================

    /**
     * 获取投递配置
     * @param export 投递信息
     */
    private ExportConfig resolveExportConfig(ExportDTO export) {
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
}
