package com.data.profile.common.enums;

/**
 * 告警接收人类型
 */
public enum AlertReceiverType {
    OWNER("owner", "任务责任人"),
    USER("user", "指定用户"),
    // 预留
    GROUP("group", "告警组"),
    ROLE("role", "角色");

    private final String code;
    private final String description;

    AlertReceiverType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AlertReceiverType of(String code) {
        if (code == null || code.isEmpty()) return null;
        for (AlertReceiverType t : values()) {
            if (t.code.equals(code)) return t;
        }
        return null;
    }
}
