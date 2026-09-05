package com.data.engine.plugin.datax;

import com.data.engine.api.DiEngineExecutor;
import com.data.engine.api.DiEngineFactory;
import com.data.engine.api.DiRequestBuilder;
import com.data.engine.plugin.datax.builder.DataXRequestBuilder;
import com.data.engine.plugin.datax.executor.DataXDiEngineExecutor;
import com.data.profile.common.config.CheckResult;
import com.data.profile.common.config.Config;

/**
 * DataX 引擎 SPI 工厂。category = "DataX"，与 SeaTunnelEngineFactory 同等地位。
 */
public class DataXEngineFactory implements DiEngineFactory {

    public static final String CATEGORY = "DataX";

    private final DiRequestBuilder requestBuilder = new DataXRequestBuilder();
    private Config config;

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CheckResult checkConfig() {
        return null;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public DiEngineExecutor getExecutor() {
        return new DataXDiEngineExecutor();
    }

    @Override
    public DiRequestBuilder getRequestBuilder() {
        return requestBuilder;
    }
}
