package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 告警发送记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertHistory {
    private Long id;
    private String historyId;
    private String taskId;
    private String instanceId;
    private String alertCondition;
    private String alertChannel;
    private String receivers;
    private String subject;
    private String content;
    /** 发送状态:0-失败,1-成功,2-跳过 */
    private Integer sendStatus;
    private String sendMessage;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;
}
