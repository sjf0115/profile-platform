package com.data.conenctor.plugin;

import com.data.connector.api.DataSourceClient;
import com.data.connector.api.Executor;
import com.data.profile.common.domain.connector.jdbc.IJdbcDataSourceInfo;
import com.data.profile.common.domain.connector.jdbc.JdbcDataSourceInfoManager;
import com.data.profile.common.domain.connector.request.ConnectorResponse;
import com.data.profile.common.domain.connector.request.ExecuteRequestParam;
import com.data.profile.common.domain.sql.ListWithQueryColumn;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.common.utils.SqlUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.Map;

public abstract class BaseJdbcExecutor implements Executor, IJdbcDataSourceInfo {

    private final DataSourceClient dataSourceClient;

    public BaseJdbcExecutor(DataSourceClient dataSourceClient) {
        this.dataSourceClient = dataSourceClient;
    }

    protected ListWithQueryColumn query(JdbcTemplate jdbcTemplate, String sql, int limit) {
        return SqlUtils.query(jdbcTemplate, sql, limit);
    }

    @Override
    public ConnectorResponse queryForPage(ExecuteRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        Map<String,String> paramMap = JSONUtils.toMap(dataSourceParam);
        if (MapUtils.isEmpty(paramMap)) {
            throw new SQLException("jdbc datasource param is no validate");
        }
        JdbcTemplate jdbcTemplate = dataSourceClient.getJdbcTemplate(
                JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(paramMap)));

        String sql = param.getScript();
        if (StringUtils.isEmpty(sql)) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("execute script must not null");
        }

        builder.result(SqlUtils.queryForPage(jdbcTemplate, sql, param.getLimit(),
                param.getPageNumber(), param.getPageSize()));

        return builder.build();
    }

    @Override
    public ConnectorResponse queryForOne(ExecuteRequestParam param) throws SQLException {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();

        Map<String,String> paramMap = JSONUtils.toMap(dataSourceParam);
        if (MapUtils.isEmpty(paramMap)) {
            throw new SQLException("jdbc datasource param is no validate");
        }

        JdbcTemplate jdbcTemplate = dataSourceClient.getJdbcTemplate(
                JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(paramMap)));

        String sql = param.getScript() + " limit 1";
        if (StringUtils.isEmpty(sql)) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("execute script must not null");
        }

        builder.result(SqlUtils.queryForPage(jdbcTemplate, sql, param.getLimit(),
                param.getPageNumber(), param.getPageSize()));

        return builder.build();
    }

    @Override
    public ConnectorResponse queryForList(ExecuteRequestParam param) throws Exception {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String dataSourceParam = param.getDataSourceParam();
        Map<String,String> paramMap = JSONUtils.toMap(dataSourceParam);
        if (MapUtils.isEmpty(paramMap)) {
            throw new SQLException("jdbc datasource param is no validate");
        }
        JdbcTemplate jdbcTemplate = dataSourceClient.getJdbcTemplate(
                JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(paramMap)));

        String sql = param.getScript();
        if (StringUtils.isEmpty(sql)) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("execute script must not null");
        }

        builder.result(query(jdbcTemplate, sql, 0));

        return builder.build();
    }
}
