package com.data.connector.plugin;

import com.data.connector.api.DataSourceClient;
import com.data.profile.common.domain.connector.jdbc.BaseJdbcDataSourceInfo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class MysqlConnector extends JdbcConnector {

    public MysqlConnector(DataSourceClient dataSourceClient) {
        super(dataSourceClient);
    }

    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(Map<String,String> param) {
        return new MysqlDataSourceInfo(param);
    }

    @Override
    public ResultSet getMetadataDatabases(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        return metaData.getCatalogs();
    }

}
