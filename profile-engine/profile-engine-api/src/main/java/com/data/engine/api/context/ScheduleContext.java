package com.data.engine.api.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调度任务中性上下文。
 *
 * <p>作为业务层（profile-web）与调度引擎插件（DolphinScheduler/Quartz/...）之间的契约，
 * 不包含任何引擎私有概念，引擎插件按需消费。</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleContext {

    /** 业务任务 ID（profile-platform 的 taskId） */
    private String taskId;

    /** 任务名称 */
    private String taskName;

    /** 调度类型: manual / day_repeat / hour_repeat */
    private String triggerType;
    // 临时过渡
    private Integer triggerTypeCode;

    /** Cron 表达式（周期调度时必填） */
    private String cronExpression;

    /** 生效开始时间 */
    private String startTime;

    /** 生效结束时间 */
    private String endTime;

    /** HTTP 回调 URL（调度引擎触发时回调此地址） */
    private String callbackUrl;
}
