package com.data.profile.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：调度配置请求
 * <p>用于配置任务调度时的轻量请求参数，替代完整的 Task 对象。</p>
 *
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConfigRequest {
    /** 调度类型: 1-手动触发, 2-日周期, 3-小时周期 */
    private Integer triggerType;
    /** Cron 表达式（周期调度时必填） */
    private String triggerCron;
    /** 生效开始时间 */
    private String triggerStartTime;
    /** 生效结束时间 */
    private String triggerEndTime;
}
