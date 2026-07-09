package com.data.profile.web.dto;

import lombok.Data;

/**
 * 功能：群组查询参数
 * 日期：2026/7/8
 */
@Data
public class GroupParam {
    // 群组名称
    private String groupName;
    // 群组状态: 1-启用, 2-停用
    private Integer groupStatus;
    // 群组类型: 1-规则筛选, 2-文件上传, 3-SQL创建
    private Integer groupType;
    // 群组主体标识ID
    private String entityIdentifierId;
    // 创建方式
    private Integer sourceType;
}
