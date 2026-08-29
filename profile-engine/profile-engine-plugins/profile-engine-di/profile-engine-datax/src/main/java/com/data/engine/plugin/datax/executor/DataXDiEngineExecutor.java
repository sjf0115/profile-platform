package com.data.engine.plugin.datax.executor;

import com.data.engine.api.DiEngineExecutor;
import com.data.engine.api.EngineJobState;
import com.data.engine.api.EngineJobStatus;
import com.data.engine.common.ExecutorRequest;
import com.data.engine.plugin.datax.core.DataXEngineProxy;

import com.data.engine.plugin.datax.helper.DataXJobBuildRequest;
import com.data.engine.plugin.datax.helper.DataXJsonHelper;
import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.profile.common.enums.engine.ExecutionStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DataX DI 引擎 Executor
 *
 * <p>生命周期：</p>
 * <ol>
 *   <li>{@link #init} 解析 {@link ExecutorRequest#getConfig()} 为 {@link DataXJobBuildRequest}，
 *       通过 {@link DataXJsonHelper} 构建 DataX JSON</li>
 *   <li>{@link #submit} 提交到 {@link DataXEngineProxy} 异步执行，立即返回作业ID</li>
 *   <li>{@link #getStatus} 轮询 Future 完成度：未完成 → RUNNING；完成 → SUCCESS/FAILED/CANCELLED</li>
 *   <li>{@link #cancel()} 调用 {@link DataXEngineProxy#cancel(String)} 发送 destroyForcibly，实现 OS 级真取消</li>
 * </ol>
 * <p>pause/restore 不支持：DataX 内核无 checkpoint 机制（接口 default 实现抛 UnsupportedOperationException）。</p>
 */
@Slf4j
public class DataXDiEngineExecutor implements DiEngineExecutor {
    private ExecutorRequest executorRequest;
    private String jobJson;
    /** 异步执行句柄：submit 后非空，不阻塞等待 */
    private CompletableFuture<ProcessResult> future;
    private final AtomicBoolean isCancel = new AtomicBoolean(false);

    @Override
    public void init(ExecutorRequest executorRequest, Configurations configurations) throws Exception {
        this.executorRequest = executorRequest;
        if (executorRequest == null) {
            throw new IllegalArgumentException("ExecutorRequest == null");
        }
        if (StringUtils.isBlank(executorRequest.getJobId())) {
            throw new IllegalArgumentException("ExecutorRequest.jobId is blank");
        }
        Map<String, Object> config = executorRequest.getConfig();
        if (config == null || config.isEmpty()) {
            throw new IllegalArgumentException("ExecutorRequest.config is empty for DataX engine");
        }
        DataXJobBuildRequest request = DataXJsonHelper.sharedMapper().convertValue(config, DataXJobBuildRequest.class);
        this.jobJson = DataXJsonHelper.buildJobJson(request);
        log.info("[DataX] init done, jobId={}", executorRequest.getJobId());
    }

    @Override
    public String submit() throws Exception {
        if (jobJson == null) {
            throw new IllegalStateException("请先初始化 DataXDiEngineExecutor");
        }
        String jobId = executorRequest.getJobId();
        log.info("异步提交 DataX 作业：{}", jobId);
        // 异步提交，不阻塞调用线程；结果通过 getStatus() 轮询
        this.future = DataXEngineProxy.getInstance().submit(jobId, jobJson);
        return jobId;
    }

    @Override
    public EngineJobStatus getStatus() throws Exception {
        if (future == null) {
            throw new IllegalStateException("请先提交 DataXDiEngineExecutor 任务");
        }
        String jobId = executorRequest.getJobId();
        if (!future.isDone()) {
            // 执行中
            return EngineJobStatus.running(jobId);
        }
        try {
            // 已完成：get() 不会阻塞；取消场景抛 CancellationException，其余异常归为 FAILED
            ProcessResult result = future.get();
            EngineJobState state = (result != null && result.isSuccess())
                    ? EngineJobState.SUCCESS : EngineJobState.FAILED;
            return EngineJobStatus.builder().engineJobId(jobId).state(state).result(result).build();
        } catch (CancellationException e) {
            ProcessResult cancelled = new ProcessResult(ExecutionStatus.FAILURE.getCode());
            cancelled.setErrorMsg("作业已取消");
            return EngineJobStatus.builder().engineJobId(jobId).state(EngineJobState.CANCELLED).result(cancelled).build();
        } catch (Throwable t) {
            log.error("[DataX] 获取作业结果异常, jobId={}", jobId, t);
            ProcessResult fail = new ProcessResult(ExecutionStatus.FAILURE.getCode());
            fail.setErrorMsg(t.getMessage());
            return EngineJobStatus.builder().engineJobId(jobId).state(EngineJobState.FAILED).result(fail).build();
        }
    }

    @Override
    public void cancel() throws Exception {
        isCancel.set(true);
        if (executorRequest != null) {
            String jobId = executorRequest.getJobId();
            // 进程模式：destroyForcibly 子进程，OS 级真取消
            DataXEngineProxy.getInstance().cancel(jobId);
        }
    }
}
