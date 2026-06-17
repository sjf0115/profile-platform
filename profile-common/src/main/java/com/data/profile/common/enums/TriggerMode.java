package com.data.profile.common.enums;

/**
 * 触发模式
 * <p>记录每次任务执行的触发来源，与 Task.triggerType（调度配置类型）分离。</p>
 */
public enum TriggerMode {
    /** 手动触发（用户点击"立即执行"） */
    MANUAL(1, "手动触发"),
    /** 定时调度触发（调度引擎回调） */
    SCHEDULED(2, "定时调度"),
    /** API 触发（外部系统调用） */
    API(3, "API触发");

    private final Integer code;
    private final String description;

    TriggerMode(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TriggerMode of(Integer code) {
        if (code == null) return null;
        for (TriggerMode mode : values()) {
            if (mode.code.equals(code)) return mode;
        }
        throw new IllegalArgumentException("未知的触发模式: " + code);
    }
}
