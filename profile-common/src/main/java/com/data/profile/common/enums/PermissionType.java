package com.data.profile.common.enums;

/**
 * 功能：权限类型枚举
 */
public enum PermissionType {
    MENU(1, "菜单"),
    BUTTON(2, "按钮"),
    API(3, "API"),
    ;

    private final Integer code;
    private final String message;

    PermissionType(Integer code, String message) {
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
