package com.data.profile.common.enums;

import lombok.Getter;

// 调度类型
@Getter
public enum TriggerType {
    // 1-无调度(手动调度),2-日周期调度,3-小时周期调度
    // 无调度
    MANUAL(1, "manual"),
    // 日周期调度
    DAY_REPEAT(2, "day"),
    // 小时周期调度
    HOUR_REPEAT(3, "hour")
    ;

    private Integer code;
    private String message;

    TriggerType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}