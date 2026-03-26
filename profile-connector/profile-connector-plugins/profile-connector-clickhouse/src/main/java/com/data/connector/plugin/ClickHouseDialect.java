package com.data.connector.plugin;

public class ClickHouseDialect extends JdbcDialect {

    @Override
    public String getDriver() {
        return "ru.yandex.clickhouse.ClickHouseDriver";
    }
}
