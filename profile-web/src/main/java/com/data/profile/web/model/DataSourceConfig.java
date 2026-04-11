package com.data.profile.web.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Map;

/**
 * 功能：数据源配置参数
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/8 23:43
 */
@Deprecated
@Data
public class DataSourceConfig {
    // 对象存储
    private String endpoint;
    private String ak;
    private String sk;
    private String bucket;
    // 数据库
    private String driver;
    private String protocol;
    private String database;
    private String host;
    private int port;
    private Map<String, String> params;
    private String url;
    @SerializedName("user_name")
    private String userName;
    private String password;
}
