package com.data.connector.plugin;

import com.data.profile.common.domain.connector.jdbc.BaseJdbcDataSourceInfo;

import java.util.Map;

public class ClickHouseDataSourceInfo extends BaseJdbcDataSourceInfo {

    public ClickHouseDataSourceInfo(Map<String,String> param) {
        super(param);
    }

    @Override
    public String getAddress() {
        return "jdbc:clickhouse" + "://" + getHost() + ":" + getPort();
    }

    @Override
    public String getDriverClass() {
        return "ru.yandex.clickhouse.ClickHouseDriver";
    }

    @Override
    public String getType() {
        return "clickhouse";
    }

    @Override
    protected String getSeparator() {
        return "?";
    }
}
