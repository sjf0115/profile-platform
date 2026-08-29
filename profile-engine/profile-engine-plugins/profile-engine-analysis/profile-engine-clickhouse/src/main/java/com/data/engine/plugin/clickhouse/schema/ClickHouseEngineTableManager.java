package com.data.engine.plugin.clickhouse.schema;

import com.data.engine.api.schema.Column;
import com.data.engine.api.schema.SchemaDiff;
import com.data.engine.api.schema.EngineTableManager;
import com.data.engine.api.schema.TableSchema;
import com.data.profile.common.enums.DataType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ClickHouse {@link EngineTableManager} 实现。
 *
 * <p>P1 阶段聚焦：</p>
 * <ul>
 *   <li>tableExists / getTableSchema 通过 system.columns 反查</li>
 *   <li>createTable 生成 MergeTree DDL（含 _sync_time / _version 系统列、ORDER BY、PARTITION BY）</li>
 *   <li>diff + alterTable 支持 add / drop / modify 三类原子操作</li>
 *   <li>中性 {@link DataType} → ClickHouse 原生类型映射（避免 N×M 笛卡尔积）</li>
 * </ul>
 */
@Slf4j
public class ClickHouseEngineTableManager implements EngineTableManager {

    /** 同步时间系统列 */
    public static final String SYS_SYNC_TIME = "_sync_time";
    /** 版本号系统列 */
    public static final String SYS_VERSION = "_version";
    /** 系统列名集合（diff 时排除） */
    private static final Set<String> SYSTEM_COLUMNS = new HashSet<>(Arrays.asList(SYS_SYNC_TIME, SYS_VERSION));

    private Map<String, Object> engineConfig;

    @Override
    public void init(Map<String, Object> engineConfig) {
        this.engineConfig = engineConfig;
    }

    // ---------------------------------------------------------------------------------------------
    // Schema 反查
    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean tableExists(String database, String tableName) throws Exception {
        String db = resolveDatabase(database);
        String sql = "SELECT 1 FROM system.tables WHERE database = ? AND name = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, db);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public TableSchema getTableSchema(String database, String tableName) throws Exception {
        String db = resolveDatabase(database);
        if (!tableExists(db, tableName)) {
            return null;
        }
        String sql = "SELECT name, type, comment FROM system.columns "
                + "WHERE database = ? AND table = ? ORDER BY position";
        List<Column> columns = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, db);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    String comment = rs.getString("comment");
                    columns.add(Column.builder()
                            .name(name)
                            .dataType(parseClickHouseType(type))
                            .comment(comment)
                            .build());
                }
            }
        }
        return TableSchema.builder()
                .database(db)
                .tableName(tableName)
                .columns(columns)
                .build();
    }

    // ---------------------------------------------------------------------------------------------
    // 建表 / 删表
    // ---------------------------------------------------------------------------------------------

    @Override
    public void createTable(TableSchema target) throws Exception {
        // TODO 转换 Freemarker 模板
        String ddl = buildCreateDdl(target);
        log.info("createTable DDL: {}", ddl);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        }
    }

    @Override
    public void dropTable(String database, String tableName) throws Exception {
        String db = resolveDatabase(database);
        String ddl = "DROP TABLE IF EXISTS " + qualified(db, tableName);
        log.info("[ClickHouse] dropTable: {}", ddl);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Schema diff & alter
    // ---------------------------------------------------------------------------------------------

    @Override
    public SchemaDiff diff(TableSchema current, TableSchema target) {
        if (target == null) {
            throw new IllegalArgumentException("target schema must not be null");
        }
        if (current == null) {
            return SchemaDiff.builder()
                    .addColumns(new ArrayList<>(target.getColumns()))
                    .build();
        }
        Map<String, Column> currentMap = current.getColumns().stream()
                .collect(Collectors.toMap(c -> c.getName().toLowerCase(), c -> c, (a, b) -> a));
        Map<String, Column> targetMap = target.getColumns().stream()
                .collect(Collectors.toMap(c -> c.getName().toLowerCase(), c -> c, (a, b) -> a));

        List<Column> add = new ArrayList<>();
        List<Column> modify = new ArrayList<>();
        for (Column tCol : target.getColumns()) {
            Column cCol = currentMap.get(tCol.getName().toLowerCase());
            if (cCol == null) {
                add.add(tCol);
            } else if (!Objects.equals(cCol.getDataType(), tCol.getDataType())
                    || !Objects.equals(StringUtils.defaultString(cCol.getComment()),
                    StringUtils.defaultString(tCol.getComment()))) {
                modify.add(tCol);
            }
        }
        List<String> drop = new ArrayList<>();
        for (Column cCol : current.getColumns()) {
            String name = cCol.getName();
            if (SYSTEM_COLUMNS.contains(name.toLowerCase())) {
                continue;
            }
            if (!targetMap.containsKey(name.toLowerCase())) {
                drop.add(name);
            }
        }
        return SchemaDiff.builder()
                .addColumns(add)
                .modifyColumns(modify)
                .dropColumns(drop)
                .build();
    }

    @Override
    public void alterTable(TableSchema target, SchemaDiff diff) throws Exception {
        if (diff == null || diff.isEmpty()) {
            log.info("[ClickHouse] alterTable skipped: no diff for {}",
                    qualified(target.getDatabase(), target.getTableName()));
            return;
        }
        String table = qualified(resolveDatabase(target.getDatabase()), target.getTableName());
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (Column c : diff.getAddColumns()) {
                String sql = "ALTER TABLE " + table + " ADD COLUMN " + columnDef(c);
                log.info("[ClickHouse] {}", sql);
                stmt.execute(sql);
            }
            for (Column c : diff.getModifyColumns()) {
                String sql = "ALTER TABLE " + table + " MODIFY COLUMN " + columnDef(c);
                log.info("[ClickHouse] {}", sql);
                stmt.execute(sql);
            }
            for (String name : diff.getDropColumns()) {
                String sql = "ALTER TABLE " + table + " DROP COLUMN `" + name + "`";
                log.info("[ClickHouse] {}", sql);
                stmt.execute(sql);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // DDL 构建
    // ---------------------------------------------------------------------------------------------

    private String buildCreateDdl(TableSchema target) {
        String db = resolveDatabase(target.getDatabase());
        StringBuilder ddl = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(qualified(db, target.getTableName())).append(" (\n");
        List<String> defs = new ArrayList<>();
        for (Column c : target.getColumns()) {
            defs.add("  " + columnDef(c));
        }
        // 系统列
        defs.add("  `" + SYS_SYNC_TIME + "` DateTime DEFAULT now() COMMENT '同步时间'");
        defs.add("  `" + SYS_VERSION + "` UInt64 DEFAULT 1 COMMENT '版本号'");
        ddl.append(String.join(",\n", defs)).append("\n) ENGINE = MergeTree() ");
        // ORDER BY
        List<String> orderBy = target.getOrderBy();
        if (!orderBy.isEmpty()) {
            ddl.append("ORDER BY (")
                    .append(orderBy.stream().map(s -> "`" + s + "`").collect(Collectors.joining(", ")))
                    .append(") ");
        } else {
            ddl.append("ORDER BY tuple() ");
        }
        // PARTITION BY
        if (StringUtils.isNotBlank(target.getPartitionBy())) {
            ddl.append("PARTITION BY ").append(target.getPartitionBy()).append(" ");
        }
        ddl.append("SETTINGS index_granularity = 8192");
        return ddl.toString();
    }

    private String columnDef(Column c) {
        StringBuilder sb = new StringBuilder("`").append(c.getName()).append("` ")
                .append(toClickHouseType(c));
        if (StringUtils.isNotBlank(c.getComment())) {
            sb.append(" COMMENT '").append(c.getComment().replace("'", "\\'")).append("'");
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------------------------------------
    // 类型映射：DataType (中性) <-> ClickHouse 原生类型
    // ---------------------------------------------------------------------------------------------

    /**
     * 中性类型 → ClickHouse 原生类型。
     *
     * <p>未来考虑 nullable 时改用 Nullable(...) 包装；P1 暂时所有列默认非空（ClickHouse 默认行为）。</p>
     */
    private String toClickHouseType(Column c) {
        DataType dt = c.getDataType();
        if (dt == null) {
            return "String";
        }
        switch (dt) {
            case BOOLEAN_TYPE:
                return "UInt8";
            case BYTE_TYPE:
                return "Int8";
            case SHORT_TYPE:
                return "Int16";
            case INT_TYPE:
                return "Int32";
            case LONG_TYPE:
                return "Int64";
            case FLOAT_TYPE:
                return "Float32";
            case DOUBLE_TYPE:
                return "Float64";
            case BIG_DECIMAL_TYPE:
                int p = c.getPrecision() == null ? 18 : c.getPrecision();
                int s = c.getScale() == null ? 4 : c.getScale();
                return "Decimal(" + p + ", " + s + ")";
            case DATE_TYPE:
                return "Date";
            case TIME_TYPE:
            case TIMESTAMP_TYPE:
                return "DateTime";
            case BYTES_TYPE:
                return "String";
            case STRING_TYPE:
            case NULL_TYPE:
            case OBJECT:
            default:
                return "String";
        }
    }

    /**
     * ClickHouse 原生类型 → 中性类型（用于 getTableSchema 反查）。
     *
     * <p>支持常见类型 + Nullable 包装识别；未识别一律 STRING_TYPE。</p>
     */
    private DataType parseClickHouseType(String chType) {
        if (StringUtils.isBlank(chType)) {
            return DataType.STRING_TYPE;
        }
        String t = chType.trim();
        // 解包 Nullable(X)
        if (t.startsWith("Nullable(") && t.endsWith(")")) {
            t = t.substring("Nullable(".length(), t.length() - 1);
        }
        // 解包 LowCardinality(X)
        if (t.startsWith("LowCardinality(") && t.endsWith(")")) {
            t = t.substring("LowCardinality(".length(), t.length() - 1);
        }
        String upper = t.toUpperCase();
        if (upper.startsWith("DECIMAL")) return DataType.BIG_DECIMAL_TYPE;
        if (upper.startsWith("FIXEDSTRING") || upper.equals("STRING")) return DataType.STRING_TYPE;
        if (upper.startsWith("DATETIME")) return DataType.TIMESTAMP_TYPE;
        switch (upper) {
            case "UINT8":
            case "INT8":
                return DataType.BYTE_TYPE;
            case "UINT16":
            case "INT16":
                return DataType.SHORT_TYPE;
            case "UINT32":
            case "INT32":
                return DataType.INT_TYPE;
            case "UINT64":
            case "INT64":
                return DataType.LONG_TYPE;
            case "FLOAT32":
                return DataType.FLOAT_TYPE;
            case "FLOAT64":
                return DataType.DOUBLE_TYPE;
            case "DATE":
            case "DATE32":
                return DataType.DATE_TYPE;
            case "BOOL":
            case "BOOLEAN":
                return DataType.BOOLEAN_TYPE;
            default:
                return DataType.STRING_TYPE;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 连接 / 工具
    // ---------------------------------------------------------------------------------------------

    private Connection getConnection() throws SQLException {
        if (engineConfig == null) {
            throw new SQLException("ClickHouseTableManager not initialized");
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
}