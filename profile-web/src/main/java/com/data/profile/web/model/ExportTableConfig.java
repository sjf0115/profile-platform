package com.data.profile.web.model;

import lombok.Data;

/**
 * 功能：投递到表
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/12/29 12:59
 */
@Data
public class ExportTableConfig extends ExportConfig {
    private String table;
}
