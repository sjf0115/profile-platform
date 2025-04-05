package com.data.profile.common.enums;

// 创建方式
public enum SourceType {
    // 系统内置
    BUILT_IN(1, "系统内置"),
    // 自定义
    CUSTOM(2, "自定义");

    private Integer code;
    private String message;

    SourceType(Integer code, String message) {
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