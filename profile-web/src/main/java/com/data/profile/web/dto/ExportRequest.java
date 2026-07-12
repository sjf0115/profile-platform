package com.data.profile.web.dto;

import lombok.Data;

/**
 * 投递创建/编辑请求
 */
@Data
public class ExportRequest {
    // 投递名称
    private String exportName;
    // 投递描述
    private String exportDesc;
    // 投递类型: 1-群组, 2-标签
    private Integer exportType;
    // 投递方式: 1-数据源, 2-应用
    private Integer exportMode;
    // 投递配置(JSON)
    private String exportConfig;
    // 调度类型: 1-手动触发, 2-API触发, 3-日周期, 4-小时周期
    private Integer schedulerType;
    // Cron表达式
    private String schedulerCron;
    // API触发URL
    private String schedulerUrl;
    // 负责人(userId)
    private String owner;
}
