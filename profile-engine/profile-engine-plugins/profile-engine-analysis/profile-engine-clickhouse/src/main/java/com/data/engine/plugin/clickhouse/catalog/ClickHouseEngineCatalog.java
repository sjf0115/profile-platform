package com.data.engine.plugin.clickhouse.catalog;

import com.data.engine.api.catalog.EngineCatalog;
import com.data.engine.plugin.clickhouse.schema.ClickHouseEngineTableManager;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.Statement;

/**
 * ClickHouse {@link EngineCatalog} 实现。
 *
 * <p>过渡期通过继承 {@link ClickHouseEngineTableManager} 复用全部 DDL / 元数据能力；
 * 额外实现 ClickHouse 专有的原子表交换能力（EXCHANGE TABLES，需 Atomic 库引擎），
 * 用于群组结果表等新旧表无空窗切换场景。</p>
 */
@Slf4j
public class ClickHouseEngineCatalog extends ClickHouseEngineTableManager implements EngineCatalog {

    @Override
    public void atomicSwap(String database, String tableA, String tableB) throws Exception {
        String db = resolveDatabase(database);
        String sql = "EXCHANGE TABLES " + qualified(db, tableA) + " AND " + qualified(db, tableB);
        log.info("[ClickHouse] atomicSwap: {}", sql);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
