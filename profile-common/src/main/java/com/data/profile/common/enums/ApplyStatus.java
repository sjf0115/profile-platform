package com.data.profile.common.enums;

/**
 * 功能：权限申请状态枚举
 */
public enum ApplyStatus {
    PENDING(1, "待审批"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已拒绝"),
    CANCELLED(4, "已取消"),
    ;

    private final Integer code;
    private final String message;

    ApplyStatus(Integer code, String message) {
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
