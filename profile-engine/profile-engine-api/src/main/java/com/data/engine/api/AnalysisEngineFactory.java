package com.data.engine.api;

import com.data.engine.api.schema.TableManager;
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
    default TableManager getTableManager() {
        return null;
    }
}
