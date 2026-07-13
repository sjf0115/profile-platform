package com.data.profile.web.dto;

import lombok.Data;

/**
 * 任务创建/编辑请求
 */
@Data
public class TaskRequest {
    // 任务名称
    private String taskName;
    // 任务描述
    private String taskDesc;
    // 任务类型: 1-群组圈选, 2-群组导出, 3-数据集
    private Integer taskType;
    // 任务关联ID
    private String taskRelatedId;
    // 调度对象ID
    private String triggerTargetId;
    // 调度类型: 1-手动触发, 2-周期调度, 3-API触发
    private Integer triggerType;
    // cron 表达式
    private String triggerCron;
    // 触发URL
    private String triggerUrl;
    // 触发开始时间
    private String triggerStartTime;
    // 触发结束时间
    private String triggerEndTime;
    // 上游任务ID列表(逗号分隔)
    private String upstreamTaskIds;
}
