package com.data.engine.plugin.executor;

import com.data.engine.api.DiEngineExecutor;
import com.data.engine.common.ExecutorRequest;
import com.data.engine.plugin.core.SeaTunnelEngineProxy;
import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

/**
 * 功能：SeaTunnelEngineExecutor
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/28 13:46
 */
@Slf4j
public class SeaTunnelDiEngineExecutor implements DiEngineExecutor {
    private String jobId;
    private String jobInstanceId;
    private String configFilePath;

    @Override
    public void init(ExecutorRequest executorRequest, Logger logger, Configurations configurations) throws Exception {
        this.configFilePath = executorRequest.getConfigPath();
        this.jobId = executorRequest.getJobId();
    }

    @Override
    public void execute() throws Exception {
        log.info("引擎执行中..........................");
        SeaTunnelEngineProxy.getInstance().executeJob(configFilePath, jobId);
    }

    @Override
    public void pause() throws Exception {
        SeaTunnelEngineProxy.getInstance().pauseJob(jobId);
    }

    @Override
    public void restore() throws Exception {
        SeaTunnelEngineProxy.getInstance().restoreJob(configFilePath, Long.parseLong(jobInstanceId), Long.parseLong(jobId));
    }

    @Override
    public void after() throws Exception {

    }

    @Override
    public void cancel() throws Exception {

    }

    @Override
    public boolean isCancel() throws Exception {
        return false;
    }

    @Override
    public ProcessResult getProcessResult() {
        return null;
    }

    @Override
    public ExecutorRequest getTaskRequest() {
        return null;
    }
}
