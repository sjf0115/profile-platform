package com.data.profile.common.enums;

// 角色类型
public enum RoleType {
    // 管理员
    ADMIN(1, "管理员"),
    // 成员
    MEMBER(2, "成员");

    private Integer code;
    private String message;

    RoleType(Integer code, String message) {
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