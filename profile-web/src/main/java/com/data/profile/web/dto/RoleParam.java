package com.data.profile.web.dto;

import lombok.Data;

/**
 * 功能：角色查询参数
 */
@Data
public class RoleParam {
    // 角色名称（模糊搜索）
    private String roleName;
    // 角色类型
    private Integer roleType;
    // 创建方式
    private Integer sourceType;
}
