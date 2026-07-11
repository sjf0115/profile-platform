package com.data.profile.common.enums;

/**
 * 报警方式
 */
public enum AlertChannel {
    SMS("sms", "短信"),
    EMAIL("email", "邮件"),
    PHONE("phone", "电话"),
    DINGTALK("dingtalk", "钉钉群机器人"),
    WEBHOOK("webhook", "WebHook");

    private final String code;
    private final String description;

    AlertChannel(String code, String description) {
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
     * 本期是否已实现（仅 EMAIL 返回 true）
     */
    public boolean isImplemented() {
        return this == EMAIL;
    }

    public static AlertChannel of(String code) {
        if (code == null || code.isEmpty()) return null;
        for (AlertChannel c : values()) {
            if (c.code.equals(code)) return c;
        }
        return null;
    }
}
