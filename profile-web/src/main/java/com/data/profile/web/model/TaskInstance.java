package com.data.profile.web.model;

import lombok.Data;

import java.util.Date;

@Data
public class TaskInstance {
    private Long id;

    private int status;

    private String instanceId;

    private String instanceName;

    private String taskId;

    private String instanceRelatedId;

    // 触发模式: 1-手动触发, 2-定时调度, 3-API触发
    private Integer triggerMode;

    private Long startTime;

    private Long endTime;

    private Long duration;

    private String message;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}