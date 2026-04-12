package com.data.engine.api;

import com.data.profile.common.config.CheckResult;
import com.data.profile.common.config.Config;
import com.data.spi.SPI;

@SPI
public interface EngineFactory {
    void prepare(RuntimeEnvironment env) throws Exception;
    void setConfig(Config config);
    Config getConfig();
    CheckResult checkConfig();
    String getCategory();
    EngineExecutor getExecutor();
}
