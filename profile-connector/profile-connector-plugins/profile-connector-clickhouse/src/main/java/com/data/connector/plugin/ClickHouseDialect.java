package com.data.connector.plugin;

import com.data.conenctor.plugin.JdbcDialect;

public class ClickHouseDialect extends JdbcDialect {

    @Override
    public String getDriver() {
        return "ru.yandex.clickhouse.ClickHouseDriver";
    }
}
