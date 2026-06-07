package com.data.engine.plugin.datax.plugin;

import com.data.engine.plugin.datax.datasource.DataXDataSource;
import com.data.engine.plugin.datax.plugin.bean.WriterContext;
import com.data.spi.SPI;

import java.util.Map;

/**
 * DataX Writer 配置构造器（一源一插件）。
 *
 * <p>每种数据源类型对应一个独立实现，通过 SPI 注册到
 * {@code META-INF/plugins/com.data.engine.plugin.datax.plugin.DataXWriterBuilder}。</p>
 *
 * @see DataXReaderBuilder
 */
@SPI
public interface DataXWriterBuilder {

    String getCategory();

    /**
     * DataX pluginName，例如 mysqlwriter / oraclewriter / hdfswriter / clickhousewriter。
     */
    String getPluginName();

    Map<String, Object> build(DataXDataSource source, WriterContext ctx);
}
