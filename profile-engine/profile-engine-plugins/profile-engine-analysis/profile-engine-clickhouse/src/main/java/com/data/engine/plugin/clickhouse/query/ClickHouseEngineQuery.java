package com.data.engine.plugin.clickhouse.query;

import com.data.engine.api.query.EngineQuery;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ClickHouse {@link EngineQuery} 实现。
 *
 * <p>交互式 SQL 纯执行通道：门面层传入完整 SQL（方言已由平台模板层适配），
 * 本实现只负责连接构建（jdbc:clickhouse 连接串收敛于此）与执行。</p>
 */
@Slf4j
public class ClickHouseEngineQuery implements EngineQuery {

    private Map<String, Object> engineConfig;

    @Override
    public void init(Map<String, Object> engineConfig) {
        this.engineConfig = engineConfig;
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) throws Exception {
        log.debug("[ClickHouse] executeQuery: {}", sql);
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                results.add(row);
            }
        }
        return results;
    }

    @Override
    public long executeCount(String sql) throws Exception {
        log.debug("[ClickHouse] executeCount: {}", sql);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0L;
    }

    @Override
    public void executeStatement(String sql) throws Exception {
        log.info("[ClickHouse] executeStatement: {}", sql);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 连接
    // ---------------------------------------------------------------------------------------------

    private Connection getConnection() throws SQLException {
        if (engineConfig == null) {
            throw new SQLException("ClickHouseEngineQuery not initialized");
        }
        String host = (String) engineConfig.get("host");
        Object portObj = engineConfig.get("port");
        int port = portObj == null ? 8123
                : (portObj instanceof Number ? ((Number) portObj).intValue()
                : Integer.parseInt(portObj.toString()));
        String database = engineConfig.get("database") == null
                ? "default" : String.valueOf(engineConfig.get("database"));
        String user = (String) engineConfig.getOrDefault("username", engineConfig.get("user"));
        String password = (String) engineConfig.get("password");
        String url = String.format("jdbc:clickhouse://%s:%d/%s", host, port, database);
        return DriverManager.getConnection(url, user, password);
    }
}
