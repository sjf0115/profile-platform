package com.data.engine.api.factory;

import com.data.engine.api.DiEngineExecutor;
import com.data.engine.api.DiRequestBuilder;
import com.data.profile.common.config.CheckResult;
import com.data.profile.common.config.Config;
import com.data.spi.SPI;

@SPI
public interface DiEngineFactory {
    void setConfig(Config config);
    Config getConfig();
    CheckResult checkConfig();
    String getCategory();
    DiEngineExecutor getExecutor();
    // 请求构建器 把中性 DiContext 转换为引擎私有的 ExecutorRequest
    DiRequestBuilder getRequestBuilder();
}
