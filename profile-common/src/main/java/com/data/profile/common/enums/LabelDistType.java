package com.data.profile.common.enums;

// 标签分布类型
public enum LabelDistType {
    // 枚举
    ENUM(1, "枚举"),
    // 非枚举
    NON_ENUM(2, "非枚举");

    private Integer code;
    private String message;

    LabelDistType(Integer code, String message) {
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