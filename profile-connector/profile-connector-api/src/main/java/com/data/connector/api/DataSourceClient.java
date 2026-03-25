package com.data.connector.api;

import com.data.profile.common.domain.connector.jdbc.BaseJdbcDataSourceInfo;
import com.data.profile.common.exception.ProfileException;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public interface DataSourceClient {

    DataSource getDataSource(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException;

    DataSource getDataSource(Map<String,Object> configMap) throws SQLException;

    DataSource getDataSource(Properties properties) throws SQLException;

    Connection getConnection(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException;

    Connection getConnection(Map<String,Object> configMap) throws SQLException;

    Connection getConnection(Map<String,Object> configMap, Logger logger) throws ProfileException;

    Connection getConnection(Properties properties) throws SQLException;

    JdbcTemplate getJdbcTemplate(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException;
}
