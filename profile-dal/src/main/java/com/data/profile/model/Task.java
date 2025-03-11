package com.data.profile.model;

import lombok.Data;

import java.util.Date;

@Data
public class Task {
    private Long id;

    private Byte status;

    private String taskId;

    private String taskName;

    private String taskDesc;

    private Integer taskType;

    private String taskRelatedId;

    private String triggerTargetId;

    private Integer triggerType;

    private String triggerCron;

    private String triggerUrl;

    private String triggerStartTime;

    private String triggerEndTime;

    private Integer sourceType;

    private String owner;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}