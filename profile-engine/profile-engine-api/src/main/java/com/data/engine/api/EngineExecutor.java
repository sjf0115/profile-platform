package com.data.engine.api;

import com.data.engine.common.ExecutorRequest;
import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.spi.SPI;
import org.slf4j.Logger;

@SPI
public interface EngineExecutor {

    // 初始化
    void init(ExecutorRequest jobExecutionRequest, Logger logger, Configurations configurations) throws Exception;

    // 执行
    void execute() throws Exception;

    // 暂停
    void pause() throws Exception;

    // 恢复
    void restore() throws Exception;

    // 取消
    void cancel() throws Exception;

    void after() throws Exception;

    boolean isCancel() throws Exception;

    ProcessResult getProcessResult();

    ExecutorRequest getTaskRequest();
}
