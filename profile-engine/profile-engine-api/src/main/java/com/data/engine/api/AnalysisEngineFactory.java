package com.data.engine.api;

import com.data.engine.api.catalog.EngineCatalog;
import com.data.engine.api.query.EngineQuery;
import com.data.engine.api.sink.EngineSink;
import com.data.engine.api.source.EngineSource;
import com.data.profile.common.config.Config;
import com.data.spi.SPI;

/**
 * 分析引擎工厂 SPI 接口。
 *
 * <p>通过 {@code PluginLoader.getPluginLoader(AnalysisEngineFactory.class).getOrCreatePlugin(category)}
 * 加载对应引擎插件（如 ClickHouse）。</p>
 * <ul>
 *   <li>{@link #getEngineCatalog()} 引擎 Catalog（DDL / 元数据）</li>
 *   <li>{@link #getEngineSink()} 引擎 Sink（DML 数据写入）</li>
 *   <li>{@link #getEngineSource()} 引擎 Source（数据流式读取）</li>
 *   <li>{@link #getEngineQuery()} 引擎 Query（交互式 SQL 执行通道）</li>
 * </ul>
 */
@SPI
public interface AnalysisEngineFactory {
    /**
     * 引擎类别标识（如 "clickhouse"），用于 SPI 路由。
     */
    String getCategory();

    void setConfig(Config config);

    Config getConfig();

    /**
     * 引擎 Catalog 负责DDL/元数据
     */
    EngineCatalog getEngineCatalog();

    /**
     * 引擎 Sink 负责DML 数据写入
     */
    EngineSink getEngineSink();

    /**
     * 引擎 Source 负责数据读取
     */
    EngineSource getEngineSource();

    /**
     * 引擎 Query 负责交互式 SQL 执行通道
     */
    EngineQuery getEngineQuery();
}
