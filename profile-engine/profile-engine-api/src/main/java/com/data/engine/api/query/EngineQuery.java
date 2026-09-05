package com.data.engine.api.query;

import java.util.List;
import java.util.Map;

/**
 * 引擎查询执行 SPI（DQL / 语句执行通道）。
 *
 * <p>与 {@link com.data.engine.api.catalog.EngineCatalog}（DDL）、
 * {@link com.data.engine.api.sink.EngineSink}（DML 写入）、
 * {@link com.data.engine.api.source.EngineSource}（流式读取）互补，
 * 承担交互式 SQL 执行职责：门面层传入完整 SQL，插件负责连接构建与执行。</p>
 *
 * <p>本接口不含业务语义与方言原语：方言 SQL 由平台模板层按引擎目录分派
 * （sql-templates/{engineType}/），插件仅作为纯执行通道。</p>
 */
public interface EngineQuery {

    /**
     * 初始化连接配置（与 EngineCatalog/EngineSink 同源 config Map，含 host/port/database/...）。
     */
    void init(Map<String, Object> engineConfig) throws Exception;

    /**
     * 执行 SELECT 查询，返回结果行列表。
     *
     * @param sql 完整 SELECT 语句（方言已由模板层适配）
     * @return 每行数据以 Map 返回，key 为列名，value 为列值
     */
    List<Map<String, Object>> executeQuery(String sql) throws Exception;

    /**
     * 执行 COUNT 类查询，返回首行首列的数值。
     *
     * @param sql 完整 COUNT 查询语句
     * @return 计数结果（无结果时返回 0）
     */
    long executeCount(String sql) throws Exception;

    /**
     * 执行 DDL/DML 语句（建表、删表、INSERT INTO ... SELECT、EXCHANGE 等）。
     *
     * @param sql 完整语句（方言已由模板层/门面语义方法适配）
     */
    void executeStatement(String sql) throws Exception;
}
