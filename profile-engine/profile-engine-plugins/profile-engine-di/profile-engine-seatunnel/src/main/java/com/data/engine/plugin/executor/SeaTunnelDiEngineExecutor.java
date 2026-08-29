package com.data.engine.plugin.executor;

import com.data.engine.api.DiEngineExecutor;
import com.data.engine.api.EngineJobState;
import com.data.engine.api.EngineJobStatus;
import com.data.engine.common.ExecutorRequest;
import com.data.engine.plugin.core.SeaTunnelEngineProxy;
import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.profile.common.enums.engine.ExecutionStatus;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

/**
 * SeaTunnel DI 引擎 Executor（提交网关模型）。
 *
 * <p>生命周期：{@link #init} 解析 configPath/jobId → {@link #submit} 异步提交集群（立即返回）→
 * {@link #getStatus} 轮询执行结果。pause/restore 委托引擎代理（需 checkpoint 支持）。</p>
 */
@Slf4j
public class SeaTunnelDiEngineExecutor implements DiEngineExecutor {
    private ExecutorRequest taskRequest;
    private String configFilePath;
    /** 异步执行句柄：submit 后非空，不阻塞等待 */
    private CompletableFuture<Void> future;

    @Override
    public void init(ExecutorRequest executorRequest, Configurations configurations) throws Exception {
        this.taskRequest = executorRequest;
        this.configFilePath = executorRequest.getConfigPath();
    }

    @Override
    public String submit() throws Exception {
        if (taskRequest == null || configFilePath == null) {
            throw new IllegalStateException("SeaTunnelDiEngineExecutor not initialized, call init() first");
        }
        String jobId = taskRequest.getJobId();
        log.info("[SeaTunnel] 提交作业, jobId={}, config={}", jobId, configFilePath);
        // 引擎代理 executeJob 为阻塞式集群提交，包装为异步执行，调用方通过 getStatus() 轮询
        this.future = CompletableFuture.runAsync(
                () -> SeaTunnelEngineProxy.getInstance().executeJob(configFilePath, jobId));
        return jobId;
    }

    @Override
    public EngineJobStatus getStatus() throws Exception {
        if (future == null) {
            throw new IllegalStateException("SeaTunnelDiEngineExecutor not submitted, call submit() first");
        }
        String jobId = taskRequest.getJobId();
        if (!future.isDone()) {
            return EngineJobStatus.running(jobId);
        }
        try {
            // 已完成：get() 不会阻塞；无异常即集群提交执行成功
            future.get();
            ProcessResult result = new ProcessResult(ExecutionStatus.SUCCESS.getCode());
            result.setSuccess(true);
            return EngineJobStatus.builder().engineJobId(jobId).state(EngineJobState.SUCCESS).result(result).build();
        } catch (Throwable t) {
            log.error("[SeaTunnel] 作业执行失败, jobId={}", jobId, t);
            ProcessResult fail = new ProcessResult(ExecutionStatus.FAILURE.getCode());
            fail.setErrorMsg(t.getMessage());
            return EngineJobStatus.builder().engineJobId(jobId).state(EngineJobState.FAILED).result(fail).build();
        }
    }

    @Override
    public void pause() throws Exception {
        SeaTunnelEngineProxy.getInstance().pauseJob(taskRequest.getJobId());
    }

    @Override
    public void restore() throws Exception {
        throw new UnsupportedOperationException(
                "SeaTunnel restore 需要 jobInstanceId 映射（待与业务实例双向映射打通后实现）");
    }
}
