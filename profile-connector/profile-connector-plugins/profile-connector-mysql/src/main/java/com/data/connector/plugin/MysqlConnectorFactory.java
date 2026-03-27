package com.data.connector.plugin;

import com.data.connector.api.*;

public class MysqlConnectorFactory extends AbstractJdbcConnectorFactory {

    @Override
    public ParameterConverter getConnectorParameterConverter() {
        return new MysqlParameterConverter();
    }

    @Override
    public Dialect getDialect() {
        return new MysqlDialect();
    }

    @Override
    public Connector getConnector() {
        return new MysqlConnector(getDataSourceClient());
    }

    @Override
    public Executor getExecutor() {
        return new MysqlExecutor(getDataSourceClient());
    }

    @Override
    public ConfigBuilder getConfigBuilder() {
        return new MysqlConfigBuilder();
    }

    @Override
    public MetricScript getMetricScript() {
        return new MysqlMetricScript();
    }
}
