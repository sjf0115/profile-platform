package com.data.profile.manager.jdbc;

import com.data.profile.model.Column;
import com.data.profile.model.ColumnBuilder;
import com.data.profile.model.JdbcParam;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;

/**
 * 功能：Jdbc 服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/8 23:25
 */
@Slf4j
@Service
public class JdbcService {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcService.class);
    private static String driverName ="org.apache.hive.jdbc.HiveDriver";
    private static String url="jdbc:hive2://localhost:10000/default";
    private static String user = "";
    private static String passwd = "";

    /**
     * 获取所有列
     * @param dbName
     * @return
     * @throws SQLException
     */
    public List<Column> getColumns(JdbcParam params, String dbName, String tableName) throws SQLException {
        List<Column> columns = Lists.newArrayList();
        if (StringUtils.isBlank(dbName) || StringUtils.isBlank(tableName)) {
            return columns;
        }

        try {
            Class.forName(params.getDriver());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(params.getUrl(), params.getUsername(), params.getPassword())) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(
                    null,    // catalog (Hive 中通常为 null)
                    dbName,      // schemaPattern (数据库名称)
                    tableName,     // tableNamePattern (表名称)
                    "%"             // columnNamePattern (通配符，匹配所有列)
            );
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME"); // 列名
                String columnType = rs.getString("TYPE_NAME"); // 数据类型
                int columnSize = rs.getInt("COLUMN_SIZE"); // 列大小
                String columnComment = rs.getString("REMARKS"); // 列备注
                Column column = new ColumnBuilder()
                        .setColumnName(columnName)
                        .setColumnType(columnType)
                        .setColumnComment(columnComment)
                        .setColumnSize(columnSize)
                        .build();
                columns.add(column);

            }
            return columns;
        }
    }

    /**
     * 获取所有表
     * @return
     * @throws SQLException
     */
    public List<String> getTables(JdbcParam params) throws SQLException {
        String databaseName = params.getDatabaseName();
        List<String> tables = Lists.newArrayList();
        if (StringUtils.isBlank(databaseName)) {
            return tables;
        }

        try {
            Class.forName(params.getDriver());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(params.getUrl(), params.getUsername(), params.getPassword())) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, databaseName, "%", new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tables.add(tableName);
            }
            return tables;
        }
    }
}
