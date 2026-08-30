package com.data.connector.plugin;

import com.data.spi.DiConfigTranslator;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JDBC 系数据源共享的同步配置翻译器。
 *
 * <p>将 JDBC 系数据源的原始 config 归一化为同步链路统一契约：
 * host / port / database / username / password / properties。
 * 字段名差异（user/username、database/schema）的知识收敛在此处，
 * mysql / postgresql / clickhouse 等 JDBC 系插件共享本实现。</p>
 *
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
public class JdbcDiConfigTranslator implements DiConfigTranslator {

    @Override
    public Map<String, Object> translate(Map<String, Object> rawConfig) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        putIfPresent(normalized, "host", firstNonNull(rawConfig, "host"));
        putIfPresent(normalized, "port", firstNonNull(rawConfig, "port"));
        putIfPresent(normalized, "database", firstNonNull(rawConfig, "database", "schema"));
        putIfPresent(normalized, "username", firstNonNull(rawConfig, "username", "user"));
        putIfPresent(normalized, "password", firstNonNull(rawConfig, "password"));
        putIfPresent(normalized, "properties", firstNonNull(rawConfig, "properties"));
        return normalized;
    }

    private Object firstNonNull(Map<String, Object> config, String... keys) {
        for (String key : keys) {
            Object value = config.get(key);
            if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
                return value;
            }
        }
        return null;
    }

    private void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }
}
