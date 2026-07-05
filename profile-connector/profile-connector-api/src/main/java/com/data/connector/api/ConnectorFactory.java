package com.data.connector.api;

import com.data.spi.SPI;

@SPI
public interface ConnectorFactory {

    // Connector 类型
    String getCategory();

    // 核心：数据源表单的动态构建配置
    ConfigBuilder getConfigBuilder();

    Connector getConnector();

    TypeConverter getTypeConverter();

    default Executor getExecutor() {
        return null;
    }

    default Dialect getDialect() {
        return null;
    }

    default ResponseConverter getResponseConverter() {
        return null;
    }

    default ParameterConverter getConnectorParameterConverter() {
        return null;
    }

    default DataSourceClient getDataSourceClient() {
        return null;
    }

    default StatementSplitter getStatementSplitter() {
        return null;
    }

    default StatementParser getStatementParser() {
        return null;
    }

    default MetricScript getMetricScript() {
        return null;
    }

    default Boolean showInFrontend() {
        return true;
    }

    default ExportConfigBuilder getExportConfigBuilder() {
        return null;
    }
}
