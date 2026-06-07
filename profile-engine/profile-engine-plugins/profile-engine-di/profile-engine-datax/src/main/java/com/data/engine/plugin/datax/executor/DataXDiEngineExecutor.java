package com.data.engine.plugin.datax.executor;

import com.data.engine.api.DiEngineExecutor;
import com.data.engine.common.ExecutorRequest;
import com.data.engine.plugin.datax.core.DataXEngineProxy;

import com.data.engine.plugin.datax.helper.DataXJobBuildRequest;
import com.data.engine.plugin.datax.helper.DataXJsonHelper;
import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.profile.common.enums.engine.ExecutionStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DataX DI 引擎 Executor。
 *
 * <p>生命周期：</p>
 * <ol>
 *   <li>{@link #init} 解析 {@link ExecutorRequest#getConfig()} 为 {@link DataXJobBuildRequest}，
 *       通过 {@link DataXJsonHelper} 构建 DataX JSON</li>
 *   <li>{@link #execute} 提交到 {@link DataXEngineProxy} 异步执行并阻塞等待结果</li>
 *   <li>{@link #pause()}/{@link #restore()} 不支持，DataX 内核无 checkpoint 机制，抛 {@link UnsupportedOperationException}</li>
 *   <li>{@link #cancel()} 调用 {@link DataXEngineProxy#cancel(String)} 发送 destroyForcibly，实现 OS 级真取消</li>
 * </ol>
 */
@Slf4j
public class DataXDiEngineExecutor implements DiEngineExecutor {

    private ExecutorRequest taskRequest;
    private String jobJson;
    private ProcessResult processResult;
    private final AtomicBoolean cancelFlag = new AtomicBoolean(false);

    @Override
    public void init(ExecutorRequest executorRequest, Logger logger, Configurations configurations) throws Exception {
        this.taskRequest = executorRequest;
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
        DataXJobBuildRequest buildReq = DataXJsonHelper.sharedMapper()
                .convertValue(config, DataXJobBuildRequest.class);
        this.jobJson = DataXJsonHelper.buildJobJson(buildReq);
        log.info("[DataX] init done, jobId={}", executorRequest.getJobId());
    }

    @Override
    public void execute() throws Exception {
        if (taskRequest == null || jobJson == null) {
            throw new IllegalStateException("DataXDiEngineExecutor not initialized, call init() first");
        }
        log.info("[DataX] 引擎执行中, jobId={}", taskRequest.getJobId());
        CompletableFuture<ProcessResult> future =
                DataXEngineProxy.getInstance().submit(taskRequest.getJobId(), jobJson);
        try {
            this.processResult = future.get();
        } catch (Throwable t) {
            log.error("[DataX] 等待任务结果异常, jobId={}", taskRequest.getJobId(), t);
            ProcessResult fail = new ProcessResult(ExecutionStatus.FAILURE.getCode());
            fail.setErrorMsg(t.getMessage());
            this.processResult = fail;
            throw t instanceof Exception ? (Exception) t : new RuntimeException(t);
        }
    }

    @Override
    public void pause() throws Exception {
        throw new UnsupportedOperationException(
                "DataX 引擎不支持 pause：DataX 内核无 checkpoint 机制，所有运行模式（standalone/local/distribute）均不支持。如需断点续传请改用 SeaTunnel 引擎。");
    }

    @Override
    public void restore() throws Exception {
        throw new UnsupportedOperationException(
                "DataX 引擎不支持 restore：DataX 内核无 savepoint 机制。如需从上次位置恢复，请业务侧通过 where 条件控制增量区间重跑。");
    }

    @Override
    public void cancel() throws Exception {
        cancelFlag.set(true);
        if (taskRequest != null) {
            // 进程模式：destroyForcibly 子进程，OS 级真取消
            DataXEngineProxy.getInstance().cancel(taskRequest.getJobId());
        }
    }

    @Override
    public void after() throws Exception {
        // no-op：资源在 DataXEngineProxy 内部 finally 已释放
    }

    @Override
    public boolean isCancel() throws Exception {
        return cancelFlag.get();
    }

    @Override
    public ProcessResult getProcessResult() {
        return processResult;
    }

    @Override
    public ExecutorRequest getTaskRequest() {
        return taskRequest;
    }
}
