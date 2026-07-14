package com.data.profile.web.dto;

import lombok.Data;

/**
 * 功能：任务实例查询参数
 * 日期：2026/7/8
 */
@Data
public class TaskInstanceParam {
    // 任务ID
    private String taskId;
    // 实例名称（模糊搜索）
    private String instanceName;
    // 状态: 1-未运行, 2-运行中, 3-运行失败, 4-运行成功
    private Integer status;
    // 实例关联ID
    private String instanceRelatedId;
    // 触发模式: 1-手动触发, 2-定时调度, 3-API触发
    private Integer triggerMode;
    // 执行时间范围-开始（时间戳毫秒）
    private Long startTimeBegin;
    // 执行时间范围-结束（时间戳毫秒）
    private Long startTimeEnd;
}
