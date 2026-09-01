package com.data.profile.web.model;

import lombok.Data;

import java.util.Date;

/**
 * 投递实体
 */
@Data
public class Export {
    private Long id;
    // 投递状态
    private Integer status;
    // 投递ID
    private String exportId;
    // 投递类型：投递群组、投递标签
    private Integer exportType;
    // 投递名称
    private String exportName;
    // 投递描述
    private String exportDesc;
    // 投递方式：1-数据源, 2-应用
    private Integer exportMode;
    // 投递配置
    private String exportConfig;

    // 调度信息
    private Integer schedulerType; // 调度类型
    private String schedulerCron; // 调度Cron表达式
    private String schedulerUrl; // 调度URL
    private Long schedulerStartTime; // 开始调度时间
    private Long schedulerEndTime; // 结束调度时间

    private Integer sourceType;
    private String owner;
    private String ownerName;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;

    // 最新任务实例（查询时关联）
    private TaskInstance latestInstance;
}