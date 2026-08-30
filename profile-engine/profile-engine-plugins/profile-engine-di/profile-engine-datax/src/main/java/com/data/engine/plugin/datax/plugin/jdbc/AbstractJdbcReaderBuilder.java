package com.data.engine.plugin.datax.plugin.jdbc;

import com.data.engine.plugin.datax.constants.DataXConstant;
import com.data.engine.plugin.datax.datasource.DataXDataSource;
import com.data.engine.plugin.datax.datasource.JdbcDataXDataSource;
import com.data.engine.plugin.datax.plugin.DataXReaderBuilder;
import com.data.engine.plugin.datax.plugin.bean.ReaderContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC 系 Reader Builder 抽象基类，封装 username/password/column/connection[{jdbcUrl, table 或 querySql}]/where/splitPk
 * 等公共字段。子类只需声明 category/pluginName，并在 {@link #customize(Map, JdbcDataXDataSource, ReaderContext)} 中
 * 处理差异化字段（如 Oracle fetchSize、ClickHouse extra props）。
 */
public abstract class AbstractJdbcReaderBuilder implements DataXReaderBuilder {

    @Override
    public final Map<String, Object> build(DataXDataSource source, ReaderContext ctx) {
        if (!(source instanceof JdbcDataXDataSource)) {
            throw new IllegalArgumentException(
                    "Expected JdbcDataXDataSource for category=" + getCategory()
                            + ", got " + (source == null ? "null" : source.getClass().getName()));
        }
        JdbcDataXDataSource jdbc = (JdbcDataXDataSource) source;
        Map<String, Object> params = new LinkedHashMap<>();

        params.put(DataXConstant.USERNAME, jdbc.getUsername());
        params.put(DataXConstant.PASSWORD, jdbc.getPassword());

        // column
        List<String> columns = ctx.getColumns();
        params.put(DataXConstant.COLUMN, CollectionUtils.isEmpty(columns) ? Collections.singletonList("*") : columns);

        // connection
        Map<String, Object> connection = new LinkedHashMap<>();
        connection.put(DataXConstant.JDBC_URL, Collections.singletonList(jdbc.getJdbcUrl()));
        if (CollectionUtils.isNotEmpty(ctx.getQuerySql())) {
            connection.put(DataXConstant.QUERY_SQL, ctx.getQuerySql());
        } else if (StringUtils.isNotBlank(ctx.getTable())) {
            connection.put(DataXConstant.TABLE, Collections.singletonList(ctx.getTable()));
        } else {
            throw new IllegalArgumentException("ReaderContext must provide either table or querySql");
        }
        List<Map<String, Object>> connections = new ArrayList<>(1);
        connections.add(connection);
        params.put(DataXConstant.CONNECTION, connections);

        // 仅在 table 模式下 where/splitPk 才有意义
        if (CollectionUtils.isEmpty(ctx.getQuerySql())) {
            if (StringUtils.isNotBlank(ctx.getWhere())) {
                params.put(DataXConstant.WHERE, ctx.getWhere());
            }
            if (StringUtils.isNotBlank(ctx.getSplitPk())) {
                params.put(DataXConstant.SPLIT_PK, ctx.getSplitPk());
            }
        }

        // 子类钩子
        customize(params, jdbc, ctx);

        // 兜底 extraParams
        if (ctx.getExtraParams() != null) {
            ctx.getExtraParams().forEach(params::putIfAbsent);
        }
        return params;
    }

    /**
     * 子类差异化扩展点；默认空实现。
     */
    protected void customize(Map<String, Object> params, JdbcDataXDataSource source, ReaderContext ctx) {
        // no-op
    }

    /**
     * 把 Map 中已有/缺省的标量字段安全设置（不覆盖已有非空值）。
     */
    protected static void putIfMissing(Map<String, Object> params, String key, Object value) {
        if (value != null && !params.containsKey(key)) {
            params.put(key, value);
        }
    }

    /** 用于子类提供默认 extraProps（避免 NPE） */
    protected static Map<String, Object> nullSafe(Map<String, Object> map) {
        return map == null ? new HashMap<>() : map;
    }
}
