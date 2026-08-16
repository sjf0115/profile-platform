package com.data.engine.api.catalog;

import com.data.engine.api.schema.TableManager;

/**
 * 引擎 Catalog（DDL / 元数据能力）。
 *
 * <p>与 {@link com.data.engine.api.sink.EngineSink}（DML）互补，
 * 承担建表、删表、存在性检查、Schema 反查与演进等职责，不做数据写入。</p>
 *
 * <p>过渡期继承 {@link TableManager} 复用既有 DDL 定义；
 * 后续可平滑扩展 listDatabases / listTables 等元数据能力，
 * 旧 TableManager 下线时将其方法内联至此。</p>
 */
public interface EngineCatalog extends TableManager {
}
