package com.data.profile.common.enums;

public enum ResponseCode {
    // 请求成功
    SUCCESS(0, "success"),

    // 用户
    // 用户不存在异常
    USER_NO_ERROR(50300, "no_user_error"),
    // 保存用户异常
    USER_SAVE_ERROR(50301, "save_user_error");

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
