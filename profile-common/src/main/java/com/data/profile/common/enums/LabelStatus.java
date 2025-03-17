package com.data.profile.common.enums;

// 标签状态
public enum LabelStatus {
    // 已创建
    CREATED(1, "created"),
    // 已绑定
    BOUNDED(2, "bounded");

    private Integer code;
    private String message;

    LabelStatus(Integer code, String message) {
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