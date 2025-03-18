package com.data.profile.common.enums;

// 标签组织类型
public enum LabelOrganizeType {
    // 单值
    SINGLE_VALUE(1, "单值"),
    // 多值
    MULTI_VALUE(2, "多值"),
    // KV
    KV(3, "KV"),
    // KKV
    KKV(4, "KKV")
    ;

    private Integer code;
    private String message;

    LabelOrganizeType(Integer code, String message) {
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
