package com.data.engine.plugin.executor;

import com.data.engine.api.EngineExecutor;
import com.data.engine.common.ExecutorRequest;
import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.ProcessResult;
import org.slf4j.Logger;

/**
 * 功能：ClickHouseEngineExecutor
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/28 13:46
 */
public class ClickHouseEngineExecutor implements EngineExecutor {
    @Override
    public void init(ExecutorRequest jobExecutionRequest, Logger logger, Configurations configurations) throws Exception {

    }

    @Override
    public void execute() throws Exception {

    }

    @Override
    public void pause() throws Exception {

    }

    @Override
    public void restore() throws Exception {

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
