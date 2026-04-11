package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户与角色关系
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {
    // 自增ID
    private Integer id;
    // 用户ID
    private String userId;
    // 角色ID
    private String roleId;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}
