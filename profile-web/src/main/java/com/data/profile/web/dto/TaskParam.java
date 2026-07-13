package com.data.profile.web.dto;

import lombok.Data;

/**
 * 任务查询参数
 */
@Data
public class TaskParam {
    // 任务名称（模糊搜索）
    private String taskName;
    // 任务类型
    private Integer taskType;
    // 调度类型
    private Integer triggerType;
    // 状态: 1-启用, 2-停用
    private Integer status;
}
