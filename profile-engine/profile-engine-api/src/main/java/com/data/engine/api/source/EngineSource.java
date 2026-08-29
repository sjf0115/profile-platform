package com.data.engine.api.source;

import com.data.engine.api.schema.Column;

import java.util.List;
import java.util.Map;

/**
 * 引擎数据读取 SPI（读侧）。
 *
 * <p>与 {@link com.data.engine.api.catalog.EngineCatalog}（DDL）、
 * {@link com.data.engine.api.sink.EngineSink}（DML 写入）对称，
 * 补齐引擎表数据读取能力，服务于跨数据源投递等数据管道场景。</p>
 *
 * <p>读取采用流式回调（{@link RowConsumer}）模式，避免全表加载内存溢出。</p>
 */
public interface EngineSource {

    /**
     * 初始化连接配置（与 EngineCatalog/EngineSink 同源 config Map，含 host/port/database/...）。
     */
    void init(Map<String, Object> engineConfig) throws Exception;

    /**
     * 反查表列元数据。
     *
     * @param database  数据库名
     * @param tableName 表名
     * @return 列定义列表（投递等场景主要使用列名，类型映射可粗化）
     */
    List<Column> describe(String database, String tableName) throws Exception;

    /**
     * 流式读取全表数据，逐行回调消费方。
     *
     * <p>实现方应使用 JDBC fetchSize 等流式机制，避免一次性加载全表。</p>
     *
     * @param database  数据库名
     * @param tableName 表名
     * @param consumer  行消费回调
     */
    void streamRows(String database, String tableName, RowConsumer consumer) throws Exception;
}
