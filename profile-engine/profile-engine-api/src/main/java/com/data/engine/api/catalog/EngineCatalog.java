package com.data.engine.api.catalog;

import com.data.engine.api.schema.EngineTableManager;

/**
 * 引擎 Catalog（DDL / 元数据能力）。
 *
 * <p>与 {@link com.data.engine.api.sink.EngineSink}（DML）互补，
 * 承担建表、删表、存在性检查、Schema 反查与演进等职责，不做数据写入。</p>
 *
 * <p>过渡期继承 {@link EngineTableManager} 复用既有 DDL 定义；
 * 后续可平滑扩展 listDatabases / listTables 等元数据能力，
 * 旧 TableManager 下线时将其方法内联至此。</p>
 */
public interface EngineCatalog extends EngineTableManager {

    /**
     * 原子交换两张表的名称（用于新旧表无空窗切换，如群组结果表发布）。
     *
     * <p>引擎能力差异大：ClickHouse 支持 EXCHANGE TABLES（Atomic 库引擎）；
     * 多数引擎不支持，默认抛出 UnsupportedOperationException，
     * 由调用方决定降级策略（如 RENAME 两步切换）。</p>
     *
     * @param database 库名
     * @param tableA   表 A
     * @param tableB   表 B
     */
    default void atomicSwap(String database, String tableA, String tableB) throws Exception {
        throw new UnsupportedOperationException("当前分析引擎不支持原子表交换（atomicSwap）");
    }
}
