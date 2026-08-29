package com.data.profile.web.engine;

import com.data.engine.api.DiEngineExecutor;
import com.data.engine.api.DiEngineFactory;
import com.data.engine.api.DiRequestBuilder;
import com.data.engine.api.EngineJobState;
import com.data.engine.api.EngineJobStatus;
import com.data.engine.api.DiContext;
import com.data.engine.common.ExecutorRequest;
import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.profile.web.config.ProfileEngineConfig;
import com.data.profile.web.model.Engine;
import com.data.profile.web.service.EngineService;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 功能：同步引擎门面（引擎门面层）
 * <p>职责：编排层与 DI 引擎插件之间的桥梁——接收中性契约 {@link DiContext}，
 * 解析引擎 → 提交作业 → 轮询至终态。不感知任何业务实体（Dataset/Export），
 * 编排层自行组装 {@link DiContext} 后调用 {@link #sync}。</p>
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

    /** 作业状态轮询间隔（毫秒） */
    private static final long POLL_INTERVAL_MS = 2000L;
    /** 心跳日志间隔（毫秒） */
    private static final long HEARTBEAT_LOG_INTERVAL_MS = 30_000L;

    @Resource
    private EngineService engineService;

    @Resource
    private ProfileEngineConfig engineProperties;

    @Resource
    private SyncEndpointResolver syncEndpointResolver;

    /**
     * 同步桥梁：将同步意图提交到默认 DI 引擎，轮询至终态，返回执行结果。
     * <p>不感知业务实体；编排层自行组装 {@link DiContext}（jobId/source/target）。
     * 引擎级参数（channel）缺省时回填平台默认值。</p>
     */
    public ProcessResult sync(DiContext context) throws Exception {
        if (context == null || context.getSource() == null || context.getTarget() == null) {
            throw new IllegalArgumentException("同步上下文缺失：source/target 端点不能为空");
        }
        if (StringUtils.isBlank(context.getJobId())) {
            throw new IllegalArgumentException("同步上下文缺失：jobId 不能为空");
        }
        // 引擎级默认参数回填（编排层未显式指定时）
        if (context.getChannel() <= 0) {
            context.setChannel(engineProperties.getSyncChannel());
        }
        if (context.getErrorRecord() <= 0) {
            context.setErrorRecord(engineProperties.getSyncErrorRecord());
        }

        // 1. 获取同步引擎
        Engine diEngine = engineService.getDefaultEngineByCategory(CATEGORY_DI);
        if (diEngine == null) {
            log.error("未找到可用的同步引擎");
            throw new IllegalStateException("未找到可用的同步引擎，请联系管理员在引擎管理中设置默认集成引擎");
        }
        String pluginName = StringUtils.lowerCase(StringUtils.trimToEmpty(diEngine.getEngineType()));
        DiEngineFactory factory = PluginLoader.getPluginLoader(DiEngineFactory.class).getOrCreatePlugin(pluginName);

        // 2. 通过引擎自带 RequestBuilder 转换为引擎私有 ExecutorRequest
        DiRequestBuilder builder = factory.getRequestBuilder();
        if (builder == null) {
            throw new IllegalStateException("引擎 [" + pluginName + "] 未实现请求构建器（DiRequestBuilder）");
        }
        ExecutorRequest executorRequest = builder.buildRequest(context);

        // 3. 提交作业（不阻塞），轮询状态至终态
        DiEngineExecutor executor = factory.getExecutor();
        executor.init(executorRequest, new Configurations());
        String engineJobId = executor.submit();
        log.info("提交同步任务成功: jobId={}, engineJobId={}, syncEngine={}",
                context.getJobId(), engineJobId, diEngine.getEngineType());
        // 轮询作业状态至终态
        ProcessResult result = awaitTerminal(executor, context.getJobId());

        // 4. 检查执行结果（终态后 result 保证非空）
        if (!result.isSuccess()) {
            throw new RuntimeException("同步失败 [" + diEngine.getEngineType() + "]: " + result.getErrorMsg());
        }
        return result;
    }

    /**
     * 门面辅助：解析默认分析引擎并翻译为目标端点。
     * <p>分析引擎的解析与 config 翻译属引擎层知识，不下放编排层。</p>
     */
    public DiContext.Endpoint resolveAnalysisTarget(String tableName, List<String> columns) {
        Engine analysisEngine = getDefaultAnalysisEngine();
        return syncEndpointResolver.resolveTarget(analysisEngine, tableName, columns);
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
     * 轮询作业状态至终态（SUCCESS/FAILED/CANCELLED），返回执行结果。
     * <p>平台只"提交 + 轮询"，不阻塞在引擎执行内部；心跳日志用于长任务可观测性。</p>
     */
    private ProcessResult awaitTerminal(DiEngineExecutor executor, String jobId) throws Exception {
        long startMs = System.currentTimeMillis();
        long lastHeartbeatMs = startMs;
        while (true) {
            EngineJobStatus status = executor.getStatus();
            if (status.getState().isTerminal()) {
                if (status.getState() == EngineJobState.CANCELLED) {
                    throw new RuntimeException("同步任务已取消: " + jobId);
                }
                ProcessResult result = status.getResult();
                if (result == null) {
                    throw new RuntimeException("同步任务终态无结果返回: " + jobId);
                }
                return result;
            }
            long now = System.currentTimeMillis();
            if (now - lastHeartbeatMs >= HEARTBEAT_LOG_INTERVAL_MS) {
                log.info("同步任务执行中: jobId={}, state={}, elapsed={}s",
                        jobId, status.getState(), (now - startMs) / 1000);
                lastHeartbeatMs = now;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
    }
}
