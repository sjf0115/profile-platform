package com.data.profile.model;

import lombok.Data;

import java.util.Map;

/**
 * 功能：Jdbc 连接信息
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/8 23:43
 */
@Data
public class JdbcParam {
    private String driver;
    private String databaseName;
    private String host;
    private int port;
    private Map<String, String> params;
    private String url;
    private String username;
    private String password;
}
