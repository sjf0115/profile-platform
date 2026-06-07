package com.data.engine.api.schema;

import java.util.Map;

/**
 * 表管理 SPI（分析引擎 Sink 侧）。
 *
 * <p>作为 {@code AnalysisEngineFactory} 的子产物（参考 SeaTunnel Catalog+SaveMode、
 * Linkis EngineConnPlugin 多产物模式），承担：</p>
 * <ul>
 *   <li>Schema 反查：{@link #tableExists}、{@link #getTableSchema}</li>
 *   <li>Schema 创建：{@link #createTable}</li>
 *   <li>Schema 演进：{@link #diff} + {@link #alterTable}</li>
 *   <li>Schema 销毁：{@link #dropTable}</li>
 * </ul>
 *
 * <p>实现类内部完成"中性 DataType → 引擎私有类型"映射，业务层零感知。</p>
 */
public interface TableManager {

    /**
     * 初始化连接配置（与 AnalysisEngineExecutor 同源 config Map，含 host/port/database/...）。
     */
    void init(Map<String, Object> engineConfig) throws Exception;

    /**
     * 表是否存在。
     */
    boolean tableExists(String database, String tableName) throws Exception;

    /**
     * 反查目标表当前 schema；若不存在返回 null。
     */
    TableSchema getTableSchema(String database, String tableName) throws Exception;

    /**
     * 按目标 schema 创建表（IF NOT EXISTS 语义由实现保障）。
     */
    void createTable(TableSchema target) throws Exception;

    /**
     * 删除表（IF EXISTS 语义由实现保障）。
     */
    void dropTable(String database, String tableName) throws Exception;

    /**
     * 计算 current → target 的 schema 差异。
     *
     * @param current 当前 schema（可空，表示目标表不存在）
     * @param target  期望 schema
     */
    SchemaDiff diff(TableSchema current, TableSchema target);

    /**
     * 应用 schema 变更（add/drop/modify column）。
     *
     * @param target 目标 schema（用于定位表 + 提供新列定义）
     * @param diff   待应用的差异
     */
    void alterTable(TableSchema target, SchemaDiff diff) throws Exception;
}
