package com.data.profile.common.enums;

import java.util.Objects;

// SelectorCondition 类型
public enum SelectorConditionType {
    // 是
    PROFILE(1, "是"),
    // 不是
    NOT_PROFILE(2, "不是"),
    // 做过
    EVENT(3, "做过"),
    // 没有做过
    NOT_EVENT(4, "没有做过"),
    // 依次做过
    EVENT_SEQUENCE(5, "依次做过"),
    // 没有依次做过
    NOT_EVENT_SEQUENCE(6, "没有依次做过")
    ;

    private Integer code;
    private String message;

    SelectorConditionType(Integer code, String message) {
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
        for (SelectorConditionType value : values()) {
            if (Objects.equals(value.code, code)) {
                return value.message;
            }
        }
        return null;
    }
}