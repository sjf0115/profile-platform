package com.data.profile.common.domain;

import com.data.profile.model.User;

/**
 * 请求上下文信息
 */
public class RequestContext {
    private static final long  serialVersionUID  = -7330741781983030806L;
    private static final ThreadLocal<RequestContext> threadLocal = new ThreadLocal<RequestContext>();
    private User user;

    private RequestContext(){
        threadLocal.set(this);
    }

    public static void remove() {
        threadLocal.remove();
    }

    public static void set(RequestContext requestContext) {
        threadLocal.set(requestContext);
    }

    public static RequestContext get() {
        RequestContext requestContext = threadLocal.get();
        if (requestContext == null) {
            return new RequestContext();
        }
        return requestContext;
    }

    // 当前登录用户ID
    public static String currentUserId() {
        User user = getCurrentUser();
        if (user != null) {
            return user.getUserId();
        }
        return "100000";
    }

    // 当前登录用户名称
    public static String currentUserName() {
        User user = getCurrentUser();
        if (user != null) {
            return user.getUserName();
        }
        return "admin";
    }

    public static void setUser(User user) {
        get().user = user;
    }

    public static User getCurrentUser() {
        return get().user;
    }
}