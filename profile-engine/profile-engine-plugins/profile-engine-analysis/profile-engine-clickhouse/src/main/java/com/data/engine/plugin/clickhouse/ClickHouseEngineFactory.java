package com.data.engine.plugin.clickhouse;

import com.data.engine.api.AnalysisEngineExecutor;
import com.data.engine.api.AnalysisEngineFactory;
import com.data.engine.api.schema.TableManager;
import com.data.engine.plugin.clickhouse.executor.ClickHouseAnalysisEngineExecutor;
import com.data.engine.plugin.clickhouse.schema.ClickHouseTableManager;
import com.data.profile.common.config.Config;

/**
 * ClickHouse 分析引擎工厂。category = "clickhouse"。
 */
public class ClickHouseEngineFactory implements AnalysisEngineFactory {

    public static final String CATEGORY = "clickhouse";

    private Config config;

    @Override
    public String getCategory() {
        return CATEGORY;
    }

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

    @Override
    public TableManager getTableManager() {
        return new ClickHouseTableManager();
    }
}