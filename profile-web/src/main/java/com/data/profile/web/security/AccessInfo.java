package com.data.profile.web.security;

import lombok.Data;

import java.util.Set;

@Data
public class AccessInfo {
    private String userName;
    private Set<String> userGroups;
    private Set<String> userRoles;
    /** 是否超级管理员（根据角色类型判断） */
    private boolean superAdmin;
    /** 权限码集合（Step 7 填充，先预留） */
    private Set<String> permissions;
}
