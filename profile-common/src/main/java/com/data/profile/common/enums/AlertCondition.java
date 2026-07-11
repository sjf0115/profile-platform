package com.data.profile.common.enums;

/**
 * 告警触发条件
 */
public enum AlertCondition {
    FAILURE("failure", "执行失败"),
    SUCCESS("success", "执行成功"),
    FINISHED("finished", "执行完成");

    private final String code;
    private final String description;

    AlertCondition(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断当前条件是否匹配实例状态
     */
    public boolean isMatch(InstanceStatus status) {
        switch (this) {
            case FAILURE:
                return status == InstanceStatus.FAILED;
            case SUCCESS:
                return status == InstanceStatus.SUCCESS;
            case FINISHED:
                return status == InstanceStatus.SUCCESS || status == InstanceStatus.FAILED;
            default:
                return false;
        }
    }

    public static AlertCondition of(String code) {
        if (code == null || code.isEmpty()) return null;
        for (AlertCondition c : values()) {
            if (c.code.equals(code)) return c;
        }
        return null;
    }
}
