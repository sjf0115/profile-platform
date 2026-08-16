package com.data.engine.plugin.clickhouse.sink;

import com.data.engine.api.schema.Column;
import com.data.engine.api.sink.EngineSink;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ClickHouse {@link EngineSink} 实现。
 *
 * <p>负责 DML：CSV 流解析写入、批量 INSERT、计数。
 * 写入采用 ClickHouse 批量 VALUES 语法，单条 SQL 分批提交。</p>
 */
@Slf4j
public class ClickHouseEngineSink implements EngineSink {

    /** 批量写入单批行数 */
    private static final int BATCH_SIZE = 1000;

    private Map<String, Object> engineConfig;

    @Override
    public void init(Map<String, Object> engineConfig) {
        this.engineConfig = engineConfig;
    }

    // ---------------------------------------------------------------------------------------------
    // CSV 流导入
    // ---------------------------------------------------------------------------------------------

    @Override
    public int importFromStream(String tableName, InputStream csvStream,
                                List<Column> columns) throws Exception {
        List<String> columnNames = new ArrayList<>();
        for (Column c : columns) {
            columnNames.add(c.getName());
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {
            // 跳过 header
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new RuntimeException("CSV 文件为空");
            }
            int total = 0;
            int lineNum = 1;
            List<Map<String, Object>> batch = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String[] parts = trimmed.split(",", -1);
                if (parts.length < columnNames.size()) {
                    throw new RuntimeException("CSV 第 " + lineNum + " 行格式错误，需要 "
                            + columnNames.size() + " 列，实际 " + parts.length + " 列");
                }
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    String value = parts[i].trim();
                    // 第一列不能为空
                    if (i == 0 && value.isEmpty()) {
                        throw new RuntimeException("CSV 第 " + lineNum + " 行第一列（" + columnNames.get(0) + "）不能为空");
                    }
                    row.put(columnNames.get(i), value);
                }
                batch.add(row);
                if (batch.size() >= BATCH_SIZE) {
                    total += batchInsert(null, tableName, batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                total += batchInsert(null, tableName, batch);
            }
            log.info("[ClickHouse] CSV 导入完成: table={}, rows={}", tableName, total);
            return total;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 批量写入 / 计数
    // ---------------------------------------------------------------------------------------------

    @Override
    public int batchInsert(String database, String tableName,
                           List<Map<String, Object>> rows) throws Exception {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        // 列顺序取第一行 keySet（契约：各行 key 集合一致）
        List<String> columns = new ArrayList<>(rows.get(0).keySet());
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(qualified(resolveDatabase(database), tableName)).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("`").append(columns.get(i)).append("`");
        }
        sb.append(") VALUES ");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("(");
            Map<String, Object> row = rows.get(i);
            for (int j = 0; j < columns.size(); j++) {
                if (j > 0) {
                    sb.append(", ");
                }
                Object value = row.get(columns.get(j));
                sb.append("'").append(escapeValue(value)).append("'");
            }
            sb.append(")");
        }
        String sql = sb.toString();
        log.debug("[ClickHouse] batchInsert: table={}, rows={}", tableName, rows.size());
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
        return rows.size();
    }

    @Override
    public long count(String database, String tableName) throws Exception {
        String sql = "SELECT COUNT(*) FROM " + qualified(resolveDatabase(database), tableName);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 连接 / 工具
    // ---------------------------------------------------------------------------------------------

    private Connection getConnection() throws SQLException {
        if (engineConfig == null) {
            throw new SQLException("ClickHouseEngineSink not initialized");
        }
        String host = (String) engineConfig.get("host");
        Object portObj = engineConfig.get("port");
        int port = portObj == null ? 8123
                : (portObj instanceof Number ? ((Number) portObj).intValue()
                : Integer.parseInt(portObj.toString()));
        String database = resolveDatabase((String) engineConfig.get("database"));
        String user = (String) engineConfig.getOrDefault("username", engineConfig.get("user"));
        String password = (String) engineConfig.get("password");
        String url = String.format("jdbc:clickhouse://%s:%d/%s", host, port, database);
        return DriverManager.getConnection(url, user, password);
    }

    private String resolveDatabase(String database) {
        if (StringUtils.isNotBlank(database)) {
            return database;
        }
        if (engineConfig != null) {
            Object db = engineConfig.get("database");
            if (db != null && StringUtils.isNotBlank(db.toString())) {
                return db.toString();
            }
        }
        return "default";
    }

    private String qualified(String database, String tableName) {
        if (StringUtils.isBlank(database)) {
            return "`" + tableName + "`";
        }
        return "`" + database + "`.`" + tableName + "`";
    }

    /**
     * 值转义：null 写空字符串，转义反斜杠和单引号。
     */
    private String escapeValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value).replace("\\", "\\\\").replace("'", "\\'");
    }
}
