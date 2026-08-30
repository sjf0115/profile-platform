package com.data.connector.api;

import com.data.spi.DiConfigTranslator;
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

    /**
     * 同步配置翻译器：把本数据源的原始 config 归一化为同步链路（DI）消费的连接配置。
     * <p>字段名知识收敛在归属插件；返回 null 表示原样直通（如 Kafka/MinIO）。</p>
     */
    default DiConfigTranslator getDiConfigTranslator() {
        return null;
    }
}
