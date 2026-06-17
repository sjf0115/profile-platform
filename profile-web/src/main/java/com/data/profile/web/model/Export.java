package com.data.profile.web.model;

import lombok.Data;

import java.util.Date;

/**
 * 投递实体
 */
@Data
public class Export {
    private Long id;

    // 投递信息
    private Integer status;
    private String exportId;
    private Integer exportType;
    private String exportName;
    private String exportDesc;
    private String exportConfig;

    // 调度信息
    private Integer schedulerType; // 调度类型
    private String schedulerCron; // 调度Cron表达式
    private String schedulerUrl; // 调度URL
    private Long schedulerStartTime; // 开始调度时间
    private Long schedulerEndTime; // 结束调度时间

    private Integer sourceType;
    private String owner;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;

    // 最新任务实例（查询时关联）
    private TaskInstance latestInstance;
}