package com.data.profile.common.enums;

/**
 * 功能：数据权限动作枚举
 */
public enum GrantAction {
    READ(1, "查看"),
    WRITE(2, "编辑"),
    EXPORT(3, "导出"),
    MANAGE(4, "管理"),
    ;

    private final Integer code;
    private final String message;

    GrantAction(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * MANAGE 隐含 READ/WRITE/EXPORT
     */
    public static boolean implies(int granted, int required) {
        if (granted == MANAGE.code) return true;
        return granted == required;
    }
}
