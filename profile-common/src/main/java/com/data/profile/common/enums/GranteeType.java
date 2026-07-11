package com.data.profile.common.enums;

/**
 * 功能：受权者类型枚举
 */
public enum GranteeType {
    USER(1, "用户"),
    ROLE(2, "角色"),
    ;

    private final Integer code;
    private final String message;

    GranteeType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
