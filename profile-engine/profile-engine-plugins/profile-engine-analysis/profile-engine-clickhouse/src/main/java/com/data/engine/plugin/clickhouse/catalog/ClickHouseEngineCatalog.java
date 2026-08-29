package com.data.engine.plugin.clickhouse.catalog;

import com.data.engine.api.catalog.EngineCatalog;
import com.data.engine.plugin.clickhouse.schema.ClickHouseEngineTableManager;

/**
 * ClickHouse {@link EngineCatalog} 实现。
 *
 * <p>过渡期通过继承 {@link ClickHouseEngineTableManager} 复用全部 DDL / 元数据能力，
 * 零新增代码；后续可在此扩展 listDatabases / listTables 等元数据能力。</p>
 */
public class ClickHouseEngineCatalog extends ClickHouseEngineTableManager implements EngineCatalog {
}
