package com.data.connector.plugin;

import com.data.connector.api.*;
import com.data.spi.DiConfigTranslator;

public abstract class AbstractJdbcConnectorFactory implements ConnectorFactory {

    @Override
    public String getCategory() {
        return "jdbc";
    }

    @Override
    public ResponseConverter getResponseConverter() {
        return new JdbcResponseConverter();
    }

    @Override
    public TypeConverter getTypeConverter() {
        return new JdbcTypeConverter();
    }

    @Override
    public ConfigBuilder getConfigBuilder() {
        return new JdbcConfigBuilder();
    }

    @Override
    public DataSourceClient getDataSourceClient() {
        return new JdbcDataSourceClient();
    }

    @Override
    public StatementSplitter getStatementSplitter() {
        return new DefaultStatementSplitter();
    }

    @Override
    public StatementParser getStatementParser() {
        return new DefaultStatementParser();
    }

    @Override
    public MetricScript getMetricScript() {
        return new JdbcMetricScript();
    }

    @Override
    public ExportConfigBuilder getExportConfigBuilder() {
        return new JdbcExportConfigBuilder();
    }

    @Override
    public DiConfigTranslator getDiConfigTranslator() {
        return new JdbcDiConfigTranslator();
    }
}
