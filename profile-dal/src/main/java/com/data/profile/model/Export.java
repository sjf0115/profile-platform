package com.data.profile.model;

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

    // 关联实例
    private String instanceId; // 最近一次实例ID
    private Integer instanceStatus; // 最近一次实例状态
    private Long instanceStartTime; // 最近一次实例开始运行时间
    private Long instanceEndTime; // 最近一次实例结束运行时间
    private Long instanceDuration; // 最近一次实例运行时长
    private String instanceMessage; // 最近一次实例运行信息
}