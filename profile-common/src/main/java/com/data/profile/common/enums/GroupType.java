package com.data.profile.common.enums;

// 群组类型
public enum GroupType {
    // 规则创建群组
    RULE(1, "rule"),
    // 文件上传群组
    UPLOAD(2, "upload"),
    // SQL创建群组
    SQL(3, "sql")
    ;

    private Integer code;
    private String message;

    GroupType(Integer code, String message) {
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