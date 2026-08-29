package com.data.connector.plugin;

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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
        JdbcTemplate jdbcTemplate = resolveJdbcTemplate(param);

        String sql = param.getScript();
        if (StringUtils.isEmpty(sql)) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("execute script must not null");
        }

        builder.result(query(jdbcTemplate, sql, 0));

        return builder.build();
    }

    // ---------------------------------------------------------------------------------------------
    // 数据写入 / 删除（投递/数据管道场景）
    // ---------------------------------------------------------------------------------------------

    @Override
    public ConnectorResponse insertData(ExecuteRequestParam param) throws Exception {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String tableName = param.getTableName();
        List<Map<String, Object>> rows = param.getRows();
        if (StringUtils.isEmpty(tableName) || rows == null || rows.isEmpty()) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("insertData 需要 tableName 与 rows");
            return builder.build();
        }
        JdbcTemplate jdbcTemplate = resolveJdbcTemplate(param);

        // 列顺序取首行 keySet（契约：各行 key 集合一致）
        List<String> columns = new ArrayList<>(rows.get(0).keySet());
        StringBuilder sb = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("`").append(columns.get(i)).append("`");
        }
        sb.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("?");
        }
        sb.append(")");

        // 参数化批量写入，防注入
        int[] results = jdbcTemplate.batchUpdate(sb.toString(), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, Object> row = rows.get(i);
                for (int j = 0; j < columns.size(); j++) {
                    Object value = row.get(columns.get(j));
                    ps.setObject(j + 1, value == null ? "" : value);
                }
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
        long total = 0;
        for (int r : results) {
            total += Math.max(r, 0);
        }
        builder.result(total);
        return builder.build();
    }

    @Override
    public ConnectorResponse deleteData(ExecuteRequestParam param) throws Exception {
        ConnectorResponse.ConnectorResponseBuilder builder = ConnectorResponse.builder();
        String sql = param.getScript();
        if (StringUtils.isEmpty(sql)) {
            builder.status(ConnectorResponse.Status.ERROR);
            builder.errorMsg("execute script must not null");
            return builder.build();
        }
        JdbcTemplate jdbcTemplate = resolveJdbcTemplate(param);
        int affected = jdbcTemplate.update(sql);
        builder.result(affected);
        return builder.build();
    }

    // ---------------------------------------------------------------------------------------------
    // 辅助方法
    // ---------------------------------------------------------------------------------------------

    /**
     * 从请求参数解析数据源连接配置并获取 JdbcTemplate。
     */
    protected JdbcTemplate resolveJdbcTemplate(ExecuteRequestParam param) throws SQLException {
        String dataSourceParam = param.getDataSourceParam();
        Map<String, String> paramMap = JSONUtils.toMap(dataSourceParam);
        if (MapUtils.isEmpty(paramMap)) {
            throw new SQLException("jdbc datasource param is no validate");
        }
        return dataSourceClient.getJdbcTemplate(
                JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(paramMap)));
    }
}
