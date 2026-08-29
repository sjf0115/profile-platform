package com.data.engine.plugin.clickhouse.source;

import com.data.engine.api.schema.Column;
import com.data.engine.api.source.EngineSource;
import com.data.engine.api.source.RowConsumer;
import com.data.profile.common.enums.DataType;
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
 * ClickHouse {@link EngineSource} 实现。
 *
 * <p>负责引擎表数据流式读取：JDBC fetchSize 流式拉取，逐行回调消费方，
 * 避免大表（如群组结果表）全量加载内存溢出。</p>
 */
@Slf4j
public class ClickHouseEngineSource implements EngineSource {

    /** 流式读取每批拉取行数 */
    private static final int FETCH_SIZE = 1000;

    private Map<String, Object> engineConfig;

    @Override
    public void init(Map<String, Object> engineConfig) {
        this.engineConfig = engineConfig;
    }

    // ---------------------------------------------------------------------------------------------
    // 列元数据反查
    // ---------------------------------------------------------------------------------------------

    @Override
    public List<Column> describe(String database, String tableName) throws Exception {
        String sql = "SELECT * FROM " + qualified(database, tableName) + " LIMIT 0";
        List<Column> columns = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                // 投递等场景主要使用列名，类型映射粗化为 STRING
                columns.add(Column.builder()
                        .name(meta.getColumnName(i))
                        .dataType(DataType.STRING_TYPE)
                        .build());
            }
        }
        return columns;
    }

    // ---------------------------------------------------------------------------------------------
    // 流式读取
    // ---------------------------------------------------------------------------------------------

    @Override
    public void streamRows(String database, String tableName, RowConsumer consumer) throws Exception {
        String sql = "SELECT * FROM " + qualified(database, tableName);
        log.info("[ClickHouse] streamRows: {}", sql);
        long total = 0;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.setFetchSize(FETCH_SIZE);
            try (ResultSet rs = stmt.executeQuery(sql)) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    consumer.accept(row);
                    total++;
                }
            }
        }
        log.info("[ClickHouse] streamRows 完成: table={}, rows={}", tableName, total);
    }

    // ---------------------------------------------------------------------------------------------
    // 连接 / 工具
    // ---------------------------------------------------------------------------------------------

    private Connection getConnection() throws SQLException {
        if (engineConfig == null) {
            throw new SQLException("ClickHouseEngineSource not initialized");
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

    private String qualified(String database, String tableName) {
        if (database == null || database.isEmpty()) {
            return "`" + tableName + "`";
        }
        return "`" + database + "`.`" + tableName + "`";
    }
}
