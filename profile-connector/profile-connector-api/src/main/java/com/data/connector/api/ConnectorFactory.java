package com.data.connector.api;

import com.data.spi.SPI;

@SPI
public interface ConnectorFactory {

    String getCategory();

    Connector getConnector();

    Executor getExecutor();

    TypeConverter getTypeConverter();

    Dialect getDialect();

    // ResponseConverter getResponseConverter();

    // ParameterConverter getConnectorParameterConverter();

    // ConfigBuilder getConfigBuilder();

    // DataSourceClient getDataSourceClient();

    // StatementSplitter getStatementSplitter();

    // StatementParser getStatementParser();

    // MetricScript getMetricScript();

    default Boolean showInFrontend() {
        return true;
    }
}
