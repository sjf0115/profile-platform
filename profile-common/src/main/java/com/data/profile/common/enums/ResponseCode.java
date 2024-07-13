package com.data.profile.common.enums;

public enum ResponseCode {
    // 请求成功
    SUCCESS(0, "success"),

    // 用户
    // 用户不存在异常
    USER_NO_ERROR(50300, "no_user_error"),
    // 保存用户异常
    USER_SAVE_ERROR(50301, "save_user_error"),

    // 标签 50400
    // 标签类目不存在
    LABEL_CATEGORY_NO_ERROR(50400, "标签类目不存在"),

    // 数据源 50500
    // 数据源不存在
    DATASOURCE_NO_ERROR(50400, "数据源不存在")


    ;

    private Integer code;
    private String message;

    ResponseCode(Integer code, String message) {
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