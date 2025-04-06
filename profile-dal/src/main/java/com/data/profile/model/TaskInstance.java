package com.data.profile.model;

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

    private Long startTime;

    private Long endTime;

    private Long duration;

    private String message;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}