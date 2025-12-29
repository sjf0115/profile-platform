package com.data.profile.manager.dao;

import lombok.Data;

/**
 * 功能：Quartz Scheduler Job
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/12/6 14:23
 */
@Data
public class SchedulerJob {
    // 任务名称
    private String jobName;
    // 任务分组
    private String jobGroup;
    // 任务执行类全路径
    private String jobClass;
    // Cron表达式
    private String cronExpression;
}
