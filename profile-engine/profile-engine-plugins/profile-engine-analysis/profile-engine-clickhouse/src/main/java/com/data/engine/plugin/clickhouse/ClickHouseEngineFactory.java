package com.data.engine.plugin.clickhouse;

import com.data.engine.api.AnalysisEngineFactory;
import com.data.engine.api.catalog.EngineCatalog;
import com.data.engine.api.query.EngineQuery;
import com.data.engine.api.sink.EngineSink;
import com.data.engine.api.source.EngineSource;
import com.data.engine.plugin.clickhouse.catalog.ClickHouseEngineCatalog;
import com.data.engine.plugin.clickhouse.query.ClickHouseEngineQuery;
import com.data.engine.plugin.clickhouse.sink.ClickHouseEngineSink;
import com.data.engine.plugin.clickhouse.source.ClickHouseEngineSource;
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
    public EngineCatalog getEngineCatalog() {
        return new ClickHouseEngineCatalog();
    }

    @Override
    public EngineSink getEngineSink() {
        return new ClickHouseEngineSink();
    }

    @Override
    public EngineSource getEngineSource() {
        return new ClickHouseEngineSource();
    }

    @Override
    public EngineQuery getEngineQuery() {
        return new ClickHouseEngineQuery();
    }
}