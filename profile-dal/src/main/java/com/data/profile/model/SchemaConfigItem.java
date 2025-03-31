package com.data.profile.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 功能：数据源配置条目
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/31 23:31
 */
@Data
public class SchemaConfigItem {
    // 展示名称
    @SerializedName("show_name")
    private String showName;
    // 配置Key
    private String key;
    // 配置Value
    private String value;
    // 是否必须 1-必填,2-选填
    private int required;
    // 是否加密展示 1-加密,2-非加密
    private int encrypt;
    // 提示
    private String tip;
}
