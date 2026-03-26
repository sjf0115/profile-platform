package com.data.connector.plugin;

import com.data.connector.plugin.AbstractJdbcConnectorFactory;
import com.data.connector.api.*;

public class ClickHouseConnectorFactory extends AbstractJdbcConnectorFactory {

    @Override
    public ParameterConverter getConnectorParameterConverter() {
        return new ClickHouseParameterConverter();
    }

    @Override
    public Dialect getDialect() {
        return new ClickHouseDialect();
    }

    @Override
    public Connector getConnector() {
        return new ClickHouseConnector(getDataSourceClient());
    }

    @Override
    public Executor getExecutor() {
        return new ClickHouseExecutor(getDataSourceClient());
    }

    @Override
    public MetricScript getMetricScript() {
        //return new ClickHouseMetricScript();
        return null;
    }

    @Override
    public ConfigBuilder getConfigBuilder() {
        return new ClickHouseConfigBuilder();
    }

    @Override
    public TypeConverter getTypeConverter() {
        return new ClickHouseTypeConverter();
    }
}
