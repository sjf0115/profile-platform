package com.data.engine.api;

import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.JobExecutionRequest;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.spi.SPI;
import org.slf4j.Logger;

@SPI
public interface EngineExecutor {

    void init(JobExecutionRequest jobExecutionRequest, Logger logger, Configurations configurations) throws Exception;

    void execute() throws Exception;

    void after() throws Exception;

    void cancel() throws Exception;

    boolean isCancel() throws Exception;

    ProcessResult getProcessResult();

    JobExecutionRequest getTaskRequest();
}
