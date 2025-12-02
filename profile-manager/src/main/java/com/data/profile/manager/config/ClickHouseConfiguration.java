package com.data.profile.manager.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * 功能：ClickHouseConfig
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/29 20:30
 */
@Configuration
public class ClickHouseConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "spring.clickhouse", ignoreUnknownFields = false)
    public ClickHouseConfig clickHouseConfig() {
        ClickHouseConfig config = new ClickHouseConfig();
        return config;
    }

    @Bean("clickHouseJdbcTemplate")
    public JdbcTemplate clickHouseJdbcTemplate(@Qualifier("clickHouseConfig") ClickHouseConfig config) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(config.getUrl());
        druidDataSource.setUsername(config.getUsername());
        druidDataSource.setPassword(config.getPassword());
        druidDataSource.setDriverClassName(config.getDriverClassName());
        return new JdbcTemplate(druidDataSource);
    }
}
