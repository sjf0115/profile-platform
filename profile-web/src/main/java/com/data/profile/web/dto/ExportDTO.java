package com.data.profile.web.dto;

import com.data.profile.web.model.TaskInstance;
import lombok.Data;

import java.util.Date;

/**
 * 投递数据传输对象（Service层返回）
 */
@Data
public class ExportDTO {
    private Long id;
    // 状态: 1-启用, 2-停用
    private Integer status;
    // 投递ID
    private String exportId;
    // 投递类型: 1-群组, 2-标签
    private Integer exportType;
    // 投递名称
    private String exportName;
    // 投递描述
    private String exportDesc;
    // 投递方式: 1-数据源, 2-应用
    private Integer exportMode;
    // 投递配置(JSON)
    private String exportConfig;
    // 调度类型
    private Integer schedulerType;
    // Cron表达式
    private String schedulerCron;
    // API触发URL
    private String schedulerUrl;
    // 开始调度时间
    private Long schedulerStartTime;
    // 结束调度时间
    private Long schedulerEndTime;
    // 创建方式: 1-系统内置, 2-自定义
    private Integer sourceType;
    // 负责人(userId)
    private String owner;
    // 负责人名称
    private String ownerName;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
    // 最新任务实例
    private TaskInstance latestInstance;
}
