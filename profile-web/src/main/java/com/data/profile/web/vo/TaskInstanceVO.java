package com.data.profile.web.vo;

import lombok.Data;

import java.util.Date;

/**
 * 任务实例视图对象
 */
@Data
public class TaskInstanceVO {
    private Long id;
    // 状态:1-未运行,2-运行中,3-运行失败,4-运行成功
    private Integer status;
    // 实例ID
    private String instanceId;
    // 实例名称
    private String instanceName;
    // 任务ID
    private String taskId;
    // 实例关联ID
    private String instanceRelatedId;
    // 触发模式: 1-手动触发, 2-定时调度, 3-API触发
    private Integer triggerMode;
    // 实例开始执行时间
    private Long startTime;
    // 实例结束执行时间
    private Long endTime;
    // 实例执行时长
    private Long duration;
    // 实例执行信息
    private String message;
    // 关联任务（嵌套对象）
    private TaskVO task;
    // 创建者
    private String creator;
    // 创建者名称
    private String creatorName;
    // 修改者
    private String modifier;
    // 修改者名称
    private String modifierName;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}
