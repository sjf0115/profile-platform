package com.data.profile.common.enums;

// 投递类型
public enum ExportType {
    // 投递群组
    GROUP(1, "group"),
    // 投递标签(用户+标签)
    LABEL(2, "label");

    private Integer code;
    private String message;

    ExportType(Integer code, String message) {
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