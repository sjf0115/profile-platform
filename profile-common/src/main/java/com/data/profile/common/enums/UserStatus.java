package com.data.profile.common.enums;

// 用户状态
public enum UserStatus {
    // 注册-未激活
    REGISTER(1, "未激活"),
    // 激活
    ACTIVATED(2, "激活"),
    // 禁用
    DISABLE(3, "停用");

    private Integer code;
    private String message;

    UserStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}