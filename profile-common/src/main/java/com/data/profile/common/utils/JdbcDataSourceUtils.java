package com.data.profile.common.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.data.profile.common.domain.connector.jdbc.BaseJdbcDataSourceInfo;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

@Slf4j
public class JdbcDataSourceUtils {

    public static void releaseConnection(Connection connection) {
        if (null != connection) {
            try {
                connection.close();
            } catch (Exception e) {
                log.error("Connection release error", e);
            }
        }
    }

    public static void closeResult(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                log.error("ResultSet close error", e);
            }
        }
    }

    public DataSource getDataSource(BaseJdbcDataSourceInfo baseJdbcDataSourceInfo) {

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(baseJdbcDataSourceInfo.getJdbcUrl());
        druidDataSource.setUsername(baseJdbcDataSourceInfo.getUser());
        druidDataSource.setPassword(StringUtils.isEmpty(baseJdbcDataSourceInfo.getPassword()) ? null : baseJdbcDataSourceInfo.getPassword());
        druidDataSource.setDriverClassName(baseJdbcDataSourceInfo.getDriverClass());
        druidDataSource.setBreakAfterAcquireFailure(true);

        return druidDataSource;
    }
}
