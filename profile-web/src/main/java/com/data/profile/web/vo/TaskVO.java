package com.data.profile.web.vo;

import lombok.Data;

import java.util.Date;

/**
 * 功能：任务
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/6/28 22:24
 */
@Data
public class TaskVO {
    // 自增ID
    private Long id;
    // 调度任务状态: 1-启用,2-停用
    private Integer status;
    // 调度任务ID
    private String taskId;
    // 调度任务名称
    private String taskName;
    // 调度任务描述
    private String taskDesc;
    // 调度任务类型:1-群组圈选,2-群组导出,3-数据集
    private Integer taskType;
    // 调度任务关联ID
    private String taskRelatedId;
    // 调度对象ID(注册调度任务)
    private String triggerTargetId;
    // 调度类型:1-手动触发调度,2-周期调度,3-API触发调度
    private Integer triggerType;
    // 调度 cron 表达式:只有周期自动触发更新才有
    private String triggerCron;
    // 调度触发URL:只有API触发调度才有
    private String triggerUrl;
    // 触发调度有效开始时间:只有周期自动触发更新才有
    private String triggerStartTime;
    // 触发调度有效结束时间:只有周期自动触发更新才有
    private String triggerEndTime;
    // 调度引擎侧的调度标识(如 DS workflowCode)
    private String scheduleId;
    // 上游任务ID列表(逗号分隔)
    private String upstreamTaskIds;
    // 创建方式: 1-系统内置,2-自定义
    private Integer sourceType;
    // 负责人
    private String owner;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}
