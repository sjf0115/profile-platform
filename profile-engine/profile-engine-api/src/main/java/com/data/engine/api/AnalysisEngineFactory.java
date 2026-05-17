package com.data.engine.api;

import com.data.profile.common.config.Config;
import com.data.spi.SPI;

@SPI
public interface AnalysisEngineFactory {
    void setConfig(Config config);
    Config getConfig();
    AnalysisEngineExecutor getExecutor();
}
