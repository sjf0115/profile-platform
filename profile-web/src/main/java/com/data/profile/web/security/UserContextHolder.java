package com.data.profile.web.security;

import com.data.profile.web.model.User;

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
}
