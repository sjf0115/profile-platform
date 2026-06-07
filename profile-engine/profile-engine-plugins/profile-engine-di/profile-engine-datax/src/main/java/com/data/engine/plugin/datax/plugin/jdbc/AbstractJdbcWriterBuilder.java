package com.data.engine.plugin.datax.plugin.jdbc;

import com.data.engine.plugin.datax.constants.DataXConstant;
import com.data.engine.plugin.datax.datasource.DataXDataSource;
import com.data.engine.plugin.datax.datasource.JdbcDataXDataSource;
import com.data.engine.plugin.datax.plugin.DataXWriterBuilder;
import com.data.engine.plugin.datax.plugin.bean.WriterContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC 系 Writer Builder 抽象基类，封装 username/password/column/connection[{jdbcUrl,table}]/preSql/postSql/writeMode/batchSize
 * 等公共字段。
 */
public abstract class AbstractJdbcWriterBuilder implements DataXWriterBuilder {

    @Override
    public final Map<String, Object> build(DataXDataSource source, WriterContext ctx) {
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
        if (CollectionUtils.isEmpty(columns)) {
            throw new IllegalArgumentException("WriterContext.columns is required");
        }
        params.put(DataXConstant.COLUMN, columns);

        // connection
        if (StringUtils.isBlank(ctx.getTable())) {
            throw new IllegalArgumentException("WriterContext.table is required");
        }
        Map<String, Object> connection = new LinkedHashMap<>();
        connection.put(DataXConstant.JDBC_URL, jdbc.getJdbcUrl());
        connection.put(DataXConstant.TABLE, Collections.singletonList(ctx.getTable()));
        List<Map<String, Object>> connections = new ArrayList<>(1);
        connections.add(connection);
        params.put(DataXConstant.CONNECTION, connections);

        // preSql/postSql
        if (CollectionUtils.isNotEmpty(ctx.getPreSql())) {
            params.put(DataXConstant.PRE_SQL, ctx.getPreSql());
        }
        if (CollectionUtils.isNotEmpty(ctx.getPostSql())) {
            params.put(DataXConstant.POST_SQL, ctx.getPostSql());
        }

        // writeMode
        params.put(DataXConstant.WRITE_MODE,
                StringUtils.isNotBlank(ctx.getWriteMode()) ? ctx.getWriteMode() : defaultWriteMode());

        // batchSize
        if (ctx.getBatchSize() != null) {
            params.put(DataXConstant.BATCH_SIZE, ctx.getBatchSize());
        }
        if (ctx.getBatchByteSize() != null) {
            params.put(DataXConstant.BATCH_BYTE_SIZE, ctx.getBatchByteSize());
        }

        customize(params, jdbc, ctx);

        if (ctx.getExtraParams() != null) {
            ctx.getExtraParams().forEach(params::putIfAbsent);
        }
        return params;
    }

    /** 子类可覆盖默认 writeMode */
    protected String defaultWriteMode() {
        return "insert";
    }

    /** 子类差异化扩展点 */
    protected void customize(Map<String, Object> params, JdbcDataXDataSource source, WriterContext ctx) {
        // no-op
    }
}
