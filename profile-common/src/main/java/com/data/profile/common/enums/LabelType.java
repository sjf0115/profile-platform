package com.data.profile.common.enums;

// 标签类型
public enum LabelType {
    // 属性标签
    ATTR(1, "属性标签"),
    // 行为标签
    BEHAVIOR(2, "行为标签");

    private Integer code;
    private String message;

    LabelType(Integer code, String message) {
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