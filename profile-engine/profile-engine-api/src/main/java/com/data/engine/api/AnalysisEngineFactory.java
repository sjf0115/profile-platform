package com.data.engine.api;

import com.data.engine.api.catalog.EngineCatalog;
import com.data.engine.api.schema.EngineTableManager;
import com.data.engine.api.sink.EngineSink;
import com.data.engine.api.source.EngineSource;
import com.data.profile.common.config.Config;
import com.data.spi.SPI;

/**
 * 分析引擎工厂 SPI 接口。
 *
 * <p>通过 {@code PluginLoader.getPluginLoader(AnalysisEngineFactory.class).getOrCreatePlugin(category)}
 * 加载对应引擎插件（如 ClickHouse）。</p>
 *
 * <p>采用单 SPI 多产物模式（参考 SeaTunnel Catalog+SaveMode、Linkis EngineConnPlugin），
 * 通过 getXxx() 返回多个子产物：</p>
 * <ul>
 *   <li>{@link #getExecutor()} 数据写入 / 查询执行器</li>
 *   <li>{@link #getTableManager()} 表 schema 管理（建表 / 演进 / 反查）</li>
 *   <li>{@link #getEngineCatalog()} 引擎 Catalog（DDL / 元数据，新体系）</li>
 *   <li>{@link #getEngineSink()} 引擎 Sink（DML 数据写入，新体系）</li>
 *   <li>{@link #getEngineSource()} 引擎 Source（数据流式读取，新体系）</li>
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

    AnalysisEngineExecutor getExecutor();

    /**
     * 表管理子产物（建表 / 演进 / 反查）。
     *
     * <p>默认返回 null 兼容旧实现；新引擎插件应当实现以支持 schema 推断与自动建表。</p>
     */
    default EngineTableManager getTableManager() {
        return null;
    }

    /**
     * 引擎 Catalog 子产物（DDL / 元数据，新体系）。
     *
     * <p>默认返回 null 兼容旧实现。</p>
     */
    default EngineCatalog getEngineCatalog() {
        return null;
    }

    /**
     * 引擎 Sink 子产物（DML 数据写入，新体系）。
     *
     * <p>默认返回 null 兼容旧实现。</p>
     */
    default EngineSink getEngineSink() {
        return null;
    }

    /**
     * 引擎 Source 子产物（数据流式读取，新体系）。
     *
     * <p>默认返回 null 兼容旧实现。</p>
     */
    default EngineSource getEngineSource() {
        return null;
    }
}
