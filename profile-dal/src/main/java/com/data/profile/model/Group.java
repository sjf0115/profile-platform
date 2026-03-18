package com.data.profile.model;

import lombok.Data;

import java.util.Date;

// 群组
@Data
public class Group {
    private Long id;
    // 群组ID
    private String groupId;
    // 群组状态: 1-启用,2-停用
    private Integer groupStatus;
    // 群组名称
    private String groupName;
    // 群组类型: 1-标签筛选,2-群组交并,3-行为圈选,4-行为序列圈选,5-组合人群,6-文件上传
    private Integer groupType;
    // 群组描述
    private String groupDesc;
    // 群组规则
    private String groupRule;
    // 群组覆盖规模
    private Integer groupCount;
    // 群组主体ID
    private String entityId;
    // 创建方式: 1-系统内置,2-自定义
    private Integer sourceType;
    // 最新执行任务实例ID
    private Integer instanceId;
    // 最新执行状态: 1-未运行,2-运行中,3-运行成功,4-运行失败
    private Integer instanceStatus;
    // 最新执行开始时间
    private Date instanceStartTime;
    // 最新执行结束时间
    private Date instanceEndTime;
    // 最新执行信息
    private String instanceMsg;
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
}
