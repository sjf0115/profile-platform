package com.data.profile.web.dto;

import com.data.profile.web.model.GroupRule;
import com.data.profile.web.vo.TaskInstanceVO;
import lombok.Data;

import java.util.Date;

/**
 * 群组聚合传输对象（Service 层出参）
 * 包含 Group DB 字段 + 关联查询字段（实体信息、调度信息、任务实例）
 */
@Data
public class GroupDTO {
    private Long id;
    private String groupId;
    private Integer groupStatus;
    private String groupName;
    private Integer groupType;
    private String groupDesc;
    private GroupRule groupRule;
    private Integer groupCount;
    private String entityIdentifierId;
    private Integer sourceType;
    private String owner;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;

    //----------------------------------------------------------
    // 调度任务查询时关联
    private String taskId;
    private Integer triggerType;
    private String triggerCron;
    private String triggerUrl;
    private String triggerStartTime;
    private String triggerEndTime;

    //----------------------------------------------------------
    // 最新任务实例（查询时关联）
    private TaskInstanceVO taskInstance;

    //----------------------------------------------------------
    // 实体
    private String entityIdentifierName;
    private String entityId;
    private String entityName;
}
