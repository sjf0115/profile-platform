package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 角色-权限关联
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RolePermission {
    private Long id;
    private String roleId;
    private String permissionId;
    private String creator;
    private Date gmtCreate;
}
