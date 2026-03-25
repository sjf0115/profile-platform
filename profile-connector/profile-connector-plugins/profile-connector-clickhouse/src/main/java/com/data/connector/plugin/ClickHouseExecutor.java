package com.data.connector.plugin;

import com.data.conenctor.plugin.BaseJdbcExecutor;
import com.data.connector.api.DataSourceClient;
import com.data.profile.common.domain.connector.jdbc.BaseJdbcDataSourceInfo;

import java.util.Map;

public class ClickHouseExecutor extends BaseJdbcExecutor {

    public ClickHouseExecutor(DataSourceClient jdbcDataSourceClient) {
        super(jdbcDataSourceClient);
    }

    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(Map<String,String> param) {
        return new ClickHouseDataSourceInfo(param);
    }
}
