package com.data.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 角色
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    // 自增ID
    private Integer id;
    // 角色ID
    private String roleId;
    // 角色类型：1-管理员,2-成员
    private Integer roleType;
    // 角色名称
    private String roleName;
    // 角色描述
    private String roleDesc;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}
