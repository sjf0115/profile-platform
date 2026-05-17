package com.data.engine.plugin;

import com.data.engine.api.AnalysisEngineExecutor;
import com.data.engine.api.AnalysisEngineFactory;
import com.data.engine.plugin.executor.ClickHouseAnalysisEngineExecutor;
import com.data.profile.common.config.Config;

/**
 * ClickHouse 分析引擎工厂
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/12 13:05
 */
public class ClickHouseEngineFactory implements AnalysisEngineFactory {
    
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
    public AnalysisEngineExecutor getExecutor() {
        return new ClickHouseAnalysisEngineExecutor();
    }
}
