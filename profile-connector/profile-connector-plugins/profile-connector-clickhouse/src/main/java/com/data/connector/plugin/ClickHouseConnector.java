package com.data.connector.plugin;

import com.data.connector.api.DataSourceClient;
import com.data.profile.common.domain.connector.jdbc.BaseJdbcDataSourceInfo;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class ClickHouseConnector extends JdbcConnector {

    public ClickHouseConnector(DataSourceClient dataSourceClient) {
        super(dataSourceClient);
    }
    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(Map<String,String> param) {
        return new ClickHouseDataSourceInfo(param);
    }

    @Override
    protected ResultSet getMetadataTables(DatabaseMetaData metaData, String catalog, String schema) throws SQLException {
        return metaData.getTables(null, catalog, null, TABLE_TYPES);
    }

    @Override
    protected ResultSet getMetadataColumns(DatabaseMetaData metaData, String catalog, String schema, String tableName, String columnName) throws SQLException {
        return metaData.getColumns(null, catalog, tableName, columnName);
    }
}
