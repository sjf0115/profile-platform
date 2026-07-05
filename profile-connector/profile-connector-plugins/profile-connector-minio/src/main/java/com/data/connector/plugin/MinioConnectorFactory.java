package com.data.connector.plugin;

import com.data.connector.api.*;

/**
 * MinIO Connector 工厂
 * <p>直接实现 ConnectorFactory（非 JDBC 体系），category 为 "minio"。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
public class MinioConnectorFactory implements ConnectorFactory {

    @Override
    public String getCategory() {
        return "minio";
    }

    @Override
    public Connector getConnector() {
        return new MinioConnector();
    }

    @Override
    public TypeConverter getTypeConverter() {
        return new MinioTypeConverter();
    }

    @Override
    public ConfigBuilder getConfigBuilder() {
        return new MinioConfigBuilder();
    }

    @Override
    public ExportConfigBuilder getExportConfigBuilder() {
        return new MinioExportConfigBuilder();
    }
}
