package com.data.connector.plugin;

import com.data.connector.api.DataSourceClient;
import com.data.profile.common.domain.connector.jdbc.BaseJdbcDataSourceInfo;
import com.data.profile.common.domain.connector.jdbc.JdbcDataSourceManager;
import com.data.profile.common.exception.ProfileException;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class JdbcDataSourceClient implements DataSourceClient {

    @Override
    public DataSource getDataSource(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(baseJdbcDataSourceInfo);
    }

    @Override
    public DataSource getDataSource(Map<String, Object> configMap) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(configMap);
    }

    @Override
    public DataSource getDataSource(Properties properties) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(properties);
    }

    @Override
    public Connection getConnection(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(baseJdbcDataSourceInfo).getConnection();
    }

    @Override
    public Connection getConnection(Map<String, Object> configMap) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(configMap).getConnection();
    }

    @Override
    public Connection getConnection(Map<String,Object> configMap, Logger logger) throws ProfileException {
        try {
            DataSource dataSource = getDataSource(configMap);
            if (dataSource != null) {
                Connection connection = dataSource.getConnection();
                logger.info("get connection success : {}",  configMap.get("url") + "[username=" + configMap.get("user") + "]");
                return connection;
            } else {
                logger.error("get datasource error");
                throw new ProfileException("can not get datasource");
            }
        } catch (SQLException exception) {
            logger.error("get connection error :", exception);
            throw new ProfileException(exception);
        }
    }

    @Override
    public Connection getConnection(Properties properties) throws SQLException {
        return JdbcDataSourceManager.getInstance().getDataSource(properties).getConnection();
    }

    @Override
    public JdbcTemplate getJdbcTemplate(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(JdbcDataSourceManager.getInstance().getDataSource(baseJdbcDataSourceInfo));
        jdbcTemplate.setFetchSize(500);
        return jdbcTemplate;
    }
}
