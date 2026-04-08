package com.data.profile.common.enums;

public enum ResponseCode {
    // 请求成功
    SUCCESS(0, "success"),
    // 请求失败
    ERROR(1, "error"),

    // 用户
    // 用户不存在异常
    USER_NO_ERROR(50300, "no_user_error"),
    // 保存用户异常
    USER_SAVE_ERROR(50301, "save_user_error"),
    // 密码验证不匹配
    USERNAME_PASSWORD_NO_MATCHED(
            50302,
            "username and password not matched or user is disabled.",
            "The user name and password do not match or user is disabled, please check your input"),
    // 验证方式不正确
    INVALID_AUTHENTICATION_PROVIDER(
            50303,
            "please provide the supported authentication providers, default PASSWD",
            "Invalid authentication provider [%s]"),

    // 标签 50400
    // 标签类目不存在
    LABEL_CATEGORY_NO_ERROR(50400, "标签类目不存在"),

    // 数据源 50500
    // 数据源不存在
    DATASOURCE_NO_ERROR(50400, "数据源不存在")

    ;

    private Integer code;
    private String message;
    private String template;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    ResponseCode(Integer code, String message, String template) {
        this.code = code;
        this.message = message;
        this.template = template;
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

    public String getTemplate() {
        return template;
    }
}