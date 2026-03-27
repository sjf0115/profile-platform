package com.data.connector.plugin;

import com.data.connector.api.DataSourceClient;
import com.data.profile.common.domain.connector.jdbc.BaseJdbcDataSourceInfo;

import java.util.Map;

public class MysqlExecutor extends BaseJdbcExecutor {

    public MysqlExecutor(DataSourceClient dataSourceClient) {
        super(dataSourceClient);
    }

    @Override
    public BaseJdbcDataSourceInfo getDatasourceInfo(Map<String,String> param) {
        return new MysqlDataSourceInfo(param);
    }
}
