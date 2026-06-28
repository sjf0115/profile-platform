package com.data.profile.web.vo;

import com.data.profile.web.model.Task;
import lombok.Data;

import java.util.Date;

/**
 * 功能：任务执行实例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/6/28 22:26
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
    // 实例创建者
    private String creator;
    // 实例修改者
    private String modifier;
    // 实例创建时间
    private Date gmtCreate;
    // 实例修改时间
    private Date gmtModified;
    // 任务详情
    private Task task;
}
