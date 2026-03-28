package com.data.connector.plugin;

import com.data.connector.api.Connector;
import com.data.connector.api.DataSourceClient;
import com.data.profile.common.domain.connector.jdbc.*;
import com.data.profile.common.domain.connector.request.*;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.common.utils.JdbcDataSourceUtils;
import com.data.profile.common.utils.StringUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.data.profile.common.domain.connector.ConfigConstants.*;


public abstract class JdbcConnector implements Connector, IJdbcDataSourceInfo {

    protected final Logger logger = LoggerFactory.getLogger(JdbcConnector.class);

    protected static final String TABLE = "TABLE";

    protected static final String VIEW = "VIEW";

    protected static final String[] TABLE_TYPES = new String[]{TABLE, VIEW};

    protected static final String TABLE_NAME = "TABLE_NAME";

    protected static final String TABLE_TYPE = "TABLE_TYPE";

    private final DataSourceClient dataSourceClient;

    public JdbcConnector(DataSourceClient dataSourceClient) {
        this.dataSourceClient = dataSourceClient;
    }

    protected Connection getConnection(String dataSourceParam, Map<String,String> param) throws SQLException {
        return dataSourceClient.getConnection(JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(param)));
    }

    @Override
    public ConnectorResponse getDatabases(GetDatabasesRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();
        Map<String,String> paramMap = JSONUtils.toMap(dataSourceParam);
        if (MapUtils.isEmpty(paramMap)) {
            throw new SQLException("jdbc datasource param is no validate");
        }

        List<DatabaseInfo> databaseList = new ArrayList<>();
        if (StringUtils.isEmptyOrNullStr(paramMap.get(DATABASE))) {
            Connection connection = getConnection(dataSourceParam, paramMap);
            ResultSet rs = getMetadataDatabases(connection);

            while (rs.next()) {
                databaseList.add(new DatabaseInfo(rs.getString(1), DATABASE));
            }
            JdbcDataSourceUtils.releaseConnection(connection);
        } else {
            databaseList.add(new DatabaseInfo(paramMap.get(DATABASE), DATABASE));
        }

        builder.result(databaseList);

        return builder.build();
    }

    @Override
    public ConnectorResponse getTables(GetTablesRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        // 解析数据源参数
        String dataSourceParam = param.getDataSourceParam();
        Map<String,String> paramMap = JSONUtils.toMap(dataSourceParam);
        if (MapUtils.isEmpty(paramMap)) {
            throw new SQLException("jdbc datasource param is no validate");
        }

        // 获取连接
        Connection connection = getConnection(dataSourceParam, paramMap);

        // 获取数据元信息
        List<TableInfo> tableList = null;
        ResultSet tables;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog;
            String schema;
            // TODO 数据库是否可以直接从数据源参数中获取
            if (StringUtils.isNotEmpty(paramMap.get(CATALOG))) {
                catalog = paramMap.get(CATALOG);
                schema = param.getDatabase();
            } else {
                catalog = param.getDatabase();
                schema = StringUtils.isEmptyOrNullStr(paramMap.get(SCHEMA))  ? null : paramMap.get(SCHEMA);
            }

            tableList = new ArrayList<>();
            tables = getMetadataTables(metaData, catalog, schema);

            if (null == tables) {
                return builder.result(tableList).build();
            }

            while (tables.next()) {
                String name = tables.getString(TABLE_NAME);
                if (!StringUtils.isEmpty(name)) {
                    String type = TABLE;
                    try {
                        type = tables.getString(TABLE_TYPE);
                    } catch (Exception e) {
                        // ignore
                    }
                    tableList.add(new TableInfo(schema, name, type, tables.getString("REMARKS")));
                }
            }

        } catch (Exception e) {
            logger.error("get table list error: ", e);
        } finally {
            JdbcDataSourceUtils.releaseConnection(connection);
        }

        return builder.result(tableList).build();
    }

    @Override
    public ConnectorResponse getColumns(GetColumnsRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();
        Map<String,String> paramMap = JSONUtils.toMap(dataSourceParam);
        if (MapUtils.isEmpty(paramMap)) {
            throw new SQLException("jdbc datasource param is no validate");
        }

        Connection connection = getConnection(dataSourceParam, paramMap);

        TableColumnInfo tableColumnInfo = null;
        try {
            String catalog;
            String schema;

            if (StringUtils.isNotEmpty(paramMap.get(CATALOG))) {
                catalog = paramMap.get(CATALOG);
                schema = param.getDataBase();
            } else {
                catalog = param.getDataBase();
                schema = StringUtils.isEmptyOrNullStr(paramMap.get(SCHEMA))  ? null : paramMap.get(SCHEMA);
            }

            String tableName = param.getTable();
            if (null != connection) {
                DatabaseMetaData metaData = connection.getMetaData();
                List<String> primaryKeys = getPrimaryKeys(catalog, schema, tableName, metaData);
                List<ColumnInfo> columns = getColumns(catalog, schema, tableName, metaData);
                tableColumnInfo = new TableColumnInfo(tableName, primaryKeys, columns);
            }
        } catch (SQLException e) {
            logger.error("get column list error , param is {} : ", param, e);
        } finally {
            JdbcDataSourceUtils.releaseConnection(connection);
        }

        return builder.result(tableColumnInfo).build();
    }

    @Override
    public ConnectorResponse getPartitions(ConnectorRequestParam param) {
        return Connector.super.getPartitions(param);
    }

    @Override
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        Map<String,String> paramMap = JSONUtils.toMap(param.getDataSourceParam());
        BaseJdbcDataSourceInfo dataSourceInfo = getDatasourceInfo(paramMap);
        dataSourceInfo.loadClass();

        try (Connection con = DriverManager.getConnection(dataSourceInfo.getJdbcUrl(), dataSourceInfo.getUser(), dataSourceInfo.getPassword())) {
            boolean result = con != null;
            if (result) {
                con.close();
            }
            return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(result).build();
        } catch (SQLException e) {
            logger.error("test connect error, param is {} :", JSONUtils.toJsonString(param), e);
            return ConnectorResponse.builder()
                    .status(ConnectorResponse.Status.ERROR)
                    .result(false)
                    .errorMsg(e.getMessage())
                    .build();
        }
    }

    private List<String> getPrimaryKeys(String catalog, String schema, String tableName, DatabaseMetaData metaData) {
        ResultSet rs = null;
        List<String> primaryKeys = new ArrayList<>();
        try {
            rs = getPrimaryKeys(metaData, catalog, schema, tableName);
            if (rs == null) {
                return primaryKeys;
            }
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            logger.error("get primary key error, param is {} :", schema + "." + tableName, e);
        } finally {
            JdbcDataSourceUtils.closeResult(rs);
        }
        return primaryKeys;
    }

    public List<ColumnInfo> getColumns(String catalog, String schema, String tableName, DatabaseMetaData metaData) {
        ResultSet rs = null;
        List<ColumnInfo> columnList = new ArrayList<>();
        try {
            rs = getMetadataColumns(metaData, catalog, schema, tableName, "%");
            if (rs == null) {
                return columnList;
            }
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                String rawType = rs.getString("TYPE_NAME");
                String comment = rs.getString("REMARKS");
                String curTableName = rs.getString("TABLE_NAME");
                // If the meta database is case-insensitive, it will identify fields that are not in the current table.
                // e.g. When querying a table named test, both the test and TEST table fields will be queried simultaneously.
                if(tableName.equals(curTableName)){
                    columnList.add(new ColumnInfo(name, rawType, comment,false));
                }
            }
        } catch (Exception e) {
            logger.error("get column error, param is {} :", schema + "." + tableName, e);
        } finally {
            JdbcDataSourceUtils.closeResult(rs);
        }
        return columnList;
    }

    @Override
    public List<String> keyProperties() {
        return Arrays.asList(HOST, PORT, DATABASE);
    }

    protected ResultSet getMetadataDatabases(Connection connection) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery("show databases");
    }

    protected ResultSet getMetadataTables(DatabaseMetaData metaData, String catalog, String schema) throws SQLException {
        return metaData.getTables(catalog, schema, null, TABLE_TYPES);
    }

    protected ResultSet getMetadataColumns(DatabaseMetaData metaData,
                                                    String catalog, String schema,
                                                    String tableName, String columnName) throws SQLException {
        return metaData.getColumns(catalog, schema, tableName, columnName);
    }

    protected ResultSet getPrimaryKeys(DatabaseMetaData metaData,String catalog, String schema, String tableName) throws SQLException {
        return metaData.getPrimaryKeys(catalog, schema, tableName);
    }
}
