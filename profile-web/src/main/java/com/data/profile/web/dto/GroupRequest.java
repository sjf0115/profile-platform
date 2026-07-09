package com.data.profile.web.dto;

import com.data.profile.web.model.GroupRule;
import lombok.Data;

/**
 * 功能：群组保存请求
 * 日期：2026/7/8
 */
@Data
public class GroupRequest {
    // 群组ID（修改时传入）
    private String groupId;
    // 群组名称
    private String groupName;
    // 群组类型: 1-规则筛选, 2-文件上传, 3-SQL创建
    private Integer groupType;
    // 群组描述
    private String groupDesc;
    // 群组规则
    private GroupRule groupRule;
    // 群组覆盖规模
    private Integer groupCount;
    // 群组主体标识ID
    private String entityIdentifierId;
    // 群组负责人
    private String owner;
    // 调度任务ID（修改调度时传入）
    private String taskId;
    // 调度类型: 1-手动触发, 2-周期调度, 3-API触发
    private Integer triggerType;
    // 调度 cron 表达式
    private String triggerCron;
    // 调度触发URL
    private String triggerUrl;
    // 触发调度有效开始时间
    private String triggerStartTime;
    // 触发调度有效结束时间
    private String triggerEndTime;
}
