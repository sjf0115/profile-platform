package com.data.engine.plugin.datax.plugin;

import com.data.engine.plugin.datax.datasource.DataXDataSource;
import com.data.engine.plugin.datax.plugin.bean.ReaderContext;
import com.data.spi.SPI;

import java.util.Map;

/**
 * DataX Reader 配置构造器（一源一插件）。
 *
 * <p>每种数据源类型（mysql/oracle/postgresql/clickhouse/hive/hdfs/...）对应一个独立实现类，
 * 通过 SPI 注册到 {@code META-INF/plugins/com.data.engine.plugin.datax.plugin.DataXReaderBuilder}：</p>
 * <pre>
 *   mysql=com.data.engine.plugin.datax.plugin.jdbc.MysqlReaderBuilder
 *   oracle=com.data.engine.plugin.datax.plugin.jdbc.OracleReaderBuilder
 *   # hive=com.data.engine.plugin.datax.plugin.hive.HiveReaderBuilder  // 扩展位
 * </pre>
 *
 * <p>JDBC 系建议继承 {@code AbstractJdbcReaderBuilder} 复用公共字段；
 * 非 JDBC（如 Hive/HDFS/OSS）直接实现本接口，pluginName 与参数自由组装。</p>
 */
@SPI
public interface DataXReaderBuilder {
    /**
     * 数据源 category，与 {@link DataXDataSource#getCategory()} 一致。
     */
    String getCategory();

    /**
     * DataX pluginName，例如 mysqlreader / oraclereader / hdfsreader / clickhousereader。
     */
    String getPluginName();

    /**
     * 构造 reader.parameter 参数 Map。
     *
     * @param source 数据源（按 category 与本 Builder 匹配；实现可强转为具体类型）
     * @param ctx    本次任务的读侧上下文
     * @return parameter 节点（不含 name 字段）
     */
    Map<String, Object> build(DataXDataSource source, ReaderContext ctx);
}