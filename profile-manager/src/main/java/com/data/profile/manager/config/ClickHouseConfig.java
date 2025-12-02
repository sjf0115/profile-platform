package com.data.profile.manager.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 功能：ClickHouse 配置类
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/11/29 23:16
 */
@Setter
@Getter
public class ClickHouseConfig {
    private String driverClassName;
    private String url;
    private String username;
    private String password;
}
