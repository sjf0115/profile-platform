package com.data.profile.web.model;

import lombok.Data;

import java.util.Date;

// 群组
@Data
public class Group {
    private Long id;
    // 群组 ID
    private String groupId;
    // 群组状态: 1-启用,2-停用
    private Integer groupStatus;
    // 群组名称
    private String groupName;
    // 群组类型: 1-规则筛选,2-文件上传,3-SQL创建
    private Integer groupType;
    // 群组描述
    private String groupDesc;
    // 群组规则
    private GroupRule groupRule;
    // 群组覆盖规模
    private Integer groupCount;
    // 群组主体标识ID
    private String entityIdentifierId;
    // 创建方式: 1-系统内置,2-自定义
    private Integer sourceType;
    // 群组负责人
    private String owner;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;

    //----------------------------------------------------------
    // 调度任务（非 DB 列，前端创建/修改群组时传入，用于注册调度）

    private String taskId;
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
}
