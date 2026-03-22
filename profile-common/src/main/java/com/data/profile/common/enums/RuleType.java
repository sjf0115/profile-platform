package com.data.profile.common.enums;

import java.util.Objects;

// 规则类型
public enum RuleType {
    // 标签
    LABEL(1, "标签"),
    // 群组
    GROUP(2, "群组"),
    // 事件
    EVENT(3, "事件"),
    // 行为序列
    SEQUENCE(4, "行为序列")
    ;

    private Integer code;
    private String message;

    RuleType(Integer code, String message) {
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
        for (RuleType value : values()) {
            if (Objects.equals(value.code, code)) {
                return value.message;
            }
        }
        return null;
    }
}