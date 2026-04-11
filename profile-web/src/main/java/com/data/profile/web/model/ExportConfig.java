package com.data.profile.web.model;

import lombok.Data;

/**
 * 功能：投递配置
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/12/29 12:57
 */
@Data
public class ExportConfig {
    // 投递目标数据源ID
    private String dataSourceId;
    // 投递的群组
    private String groupId;
}
