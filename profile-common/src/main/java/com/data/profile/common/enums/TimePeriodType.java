package com.data.profile.common.enums;

import java.util.Objects;

// 时间区间类型
public enum TimePeriodType {
    TODAY(1, "今天"),
    YESTERDAY(2, "昨天"),
    THIS_WEEK(3, "本周"),
    LAST_WEEK(4, "上周"),
    THIS_MONTH(5, "本月"),
    LAST_MONTH(6, "上月"),
    THIS_YEAR(7, "今年"),
    LAST_YEAR(8, "去年"),
    LAST_5_DAYS(9, "过去5天"),
    LAST_7_DAYS(10, "过去7天"),
    LAST_14_DAYS(11, "过去14天"),
    LAST_30_DAYS(12, "过去30天"),
    LAST_60_DAYS(13, "过去60天"),
    LAST_90_DAYS(14, "过去90天"),
    LAST_180_DAYS(15, "过去180天"),
    CUSTOM(16, "自定义");

    private Integer code;
    private String message;

    TimePeriodType(Integer code, String message) {
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

    public static String codeOf(Integer code) {
        for (TimePeriodType value : values()) {
            if (Objects.equals(value.code, code)) {
                return value.message;
            }
        }
        return null;
    }
}