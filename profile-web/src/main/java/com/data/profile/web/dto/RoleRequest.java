package com.data.profile.web.dto;

import lombok.Data;

/**
 * 功能：角色保存请求
 */
@Data
public class RoleRequest {
    // 角色ID（修改时传入）
    private String roleId;
    // 角色类型：1-管理员,2-成员
    private Integer roleType;
    // 角色名称
    private String roleName;
    // 角色描述
    private String roleDesc;
}
