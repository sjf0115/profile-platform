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
    // 创建方式: 1-系统内置,2-自定义
    private Integer sourceType;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}
