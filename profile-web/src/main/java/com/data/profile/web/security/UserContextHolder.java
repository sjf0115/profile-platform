package com.data.profile.web.security;

import com.data.profile.web.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class UserContextHolder {
    private static final ThreadLocal<UserContext> userContextHolder = new ThreadLocal<>();

    public static void setUserContext(UserContext userContext) {
        userContextHolder.set(userContext);
    }

    public static User getUser() {
        UserContext userContext = getUserContext();
        return userContext.getUser();
    }

    public static AccessInfo getAccessInfo() {
        UserContext userContext = getUserContext();
        return userContext.getAccessInfo();
    }

    public static UserContext getUserContext() {
        UserContext userContext = userContextHolder.get();
        if (userContext == null) {
            throw new RuntimeException("User context not found, please login first");
        }
        return userContext;
    }

    /**
     * 获取当前用户ID（便捷方法）
     */
    public static String currentUserId() {
        return getUser().getUserId();
    }

    /**
     * 获取当前用户名（便捷方法）
     */
    public static String currentUserName() {
        return getUser().getUserName();
    }

    public static void clear() {
        userContextHolder.remove();
    }

    /**
     * 判断当前用户是否拥有指定权限码
     */
    public static boolean hasPermission(String code) {
        AccessInfo accessInfo = getAccessInfo();
        if (accessInfo.isSuperAdmin()) {
            return true;
        }
        Set<String> permissions = accessInfo.getPermissions();
        return permissions != null && permissions.contains(code);
    }

    /**
     * 当前用户是否超级管理员
     */
    public static boolean isSuperAdmin() {
        return getAccessInfo().isSuperAdmin();
    }

    /**
     * 获取当前用户角色ID列表
     */
    public static Set<String> currentRoleIds() {
        Set<String> roles = getAccessInfo().getUserRoles();
        return roles != null ? roles : Collections.emptySet();
    }
}
