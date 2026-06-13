package com.data.engine.plugin.datax.datasource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;

/**
 * DataX 数据源抽象（解耦关键）。
 *
 * <p>本接口与 {@code profile-connector} 模块解耦：上层（如 profile-web）应负责把
 * Connector 的运行时数据源对象适配成 {@link DataXDataSource} 子类，引擎模块仅消费该抽象。</p>
 *
 * <p>常见实现：</p>
 * <ul>
 *     <li>{@link JdbcDataXDataSource}：所有 JDBC 类（mysql / oracle / postgresql / clickhouse / ...）</li>
 *     <li>{@link HiveDataXDataSource}：Hive（基于 HDFS + metastore）</li>
 * </ul>
 *
 * @see com.data.engine.plugin.datax.plugin.DataXReaderBuilder
 * @see com.data.engine.plugin.datax.plugin.DataXWriterBuilder
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JdbcDataXDataSource.class, name = "jdbc"),
        @JsonSubTypes.Type(value = HiveDataXDataSource.class, name = "hive"),
        @JsonSubTypes.Type(value = HdfsDataXDataSource.class, name = "hdfs")
})
public interface DataXDataSource {

    /**
     * 数据源类别（与 ConnectorFactory#getCategory 一致：mysql / oracle / hive / hdfs / ...）。
     * Reader/Writer Builder 通过该值经 SPI 路由。
     */
    String getCategory();

    /**
     * 兜底原始参数（提供给 Builder 在通用字段无法覆盖的扩展场景使用）。
     */
    Map<String, Object> toRawParams();
}
