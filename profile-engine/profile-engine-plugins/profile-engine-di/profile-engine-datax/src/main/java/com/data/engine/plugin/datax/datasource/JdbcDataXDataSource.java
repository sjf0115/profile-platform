package com.data.engine.plugin.datax.datasource;

import com.data.profile.common.domain.connector.jdbc.BaseJdbcDataSourceInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBC 系数据源（mysql / oracle / postgresql / clickhouse / ...）。
 *
 * <p>由上层（profile-web）从 Connector 的 {@link BaseJdbcDataSourceInfo} 适配而来；
 * 也可由调用方直接 builder 构造。</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JdbcDataXDataSource implements DataXDataSource {

    /** 数据源类别（mysql / oracle / postgresql / clickhouse / ...） */
    private String category;
    private String jdbcUrl;
    private String driverClass;
    private String username;
    private String password;
    /** 扩展参数（如 Oracle fetchSize、ClickHouse compress 等） */
    private Map<String, Object> extraProps;

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public Map<String, Object> toRawParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("category", category);
        params.put("jdbcUrl", jdbcUrl);
        params.put("driverClass", driverClass);
        params.put("username", username);
        params.put("password", password);
        if (extraProps != null) {
            params.putAll(extraProps);
        }
        return params;
    }

    /**
     * 从 Connector 的 {@link BaseJdbcDataSourceInfo} 适配出 DataX 数据源。
     */
    public static JdbcDataXDataSource fromBaseJdbc(String category, BaseJdbcDataSourceInfo info) {
        return JdbcDataXDataSource.builder()
                .category(category)
                .jdbcUrl(info.getJdbcUrl())
                .driverClass(info.getDriverClass())
                .username(info.getUser())
                .password(info.getPassword())
                .extraProps(Collections.emptyMap())
                .build();
    }
}
