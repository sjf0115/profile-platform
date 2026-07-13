package com.data.profile.web.dto;

import lombok.Data;

import java.util.Date;

/**
 * 任务数据传输对象
 */
@Data
public class TaskDTO {
    private Long id;
    private Integer status;
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
    private String scheduleId;
    private String upstreamTaskIds;
    private String alertCondition;
    private String alertChannels;
    private String alertReceivers;
    private Integer sourceType;
    private String owner;
    private String creator;
    private String creatorName;
    private String modifier;
    private String modifierName;
    private Date gmtCreate;
    private Date gmtModified;
}
