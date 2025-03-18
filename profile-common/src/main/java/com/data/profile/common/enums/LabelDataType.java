package com.data.profile.common.enums;

// 标签数据类型
public enum LabelDataType {
    // 文本型
    TEXT(1, "文本型"),
    // 数值型
    NUMBER(2, "数值型"),
    // 数值型
    DATETIME(3, "时间型")
    ;

    private Integer code;
    private String message;

    LabelDataType(Integer code, String message) {
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