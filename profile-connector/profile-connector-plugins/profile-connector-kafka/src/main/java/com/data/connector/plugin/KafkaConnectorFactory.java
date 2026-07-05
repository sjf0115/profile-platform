package com.data.connector.plugin;

import com.data.connector.api.*;

/**
 * Kafka Connector 工厂
 * <p>直接实现 ConnectorFactory（非 JDBC 体系），category 为 "kafka"。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
public class KafkaConnectorFactory implements ConnectorFactory {

    @Override
    public String getCategory() {
        return "kafka";
    }

    @Override
    public Connector getConnector() {
        return new KafkaConnector();
    }

    @Override
    public TypeConverter getTypeConverter() {
        return new KafkaTypeConverter();
    }

    @Override
    public ConfigBuilder getConfigBuilder() {
        return new KafkaConfigBuilder();
    }
}
