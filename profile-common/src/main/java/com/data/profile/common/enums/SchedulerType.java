package com.data.profile.common.enums;

// 调度类型
public enum SchedulerType {
    // 1-手动触发调度,2-API触发调度,3-日周期调度,4-小时周期调度
    // 手动触发调度
    MANUAL(1, "manual"),
    // API触发调度
    API(2, "api"),
    // 日周期调度
    DAY_REPEAT(3, "day"),
    // 小时周期调度
    HOUR_REPEAT(4, "hour")
    ;

    private Integer code;
    private String message;

    SchedulerType(Integer code, String message) {
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