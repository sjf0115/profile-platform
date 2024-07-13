package com.data.profile.common.enums;

// 通用状态
public enum Status {
    // 启用
    ENABLE(1, "enable"),
    // 禁用
    DISABLE(2, "disable");

    private Integer code;
    private String message;

    Status(Integer code, String message) {
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