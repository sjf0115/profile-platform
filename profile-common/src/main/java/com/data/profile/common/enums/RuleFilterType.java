package com.data.profile.common.enums;

import java.util.Objects;

// 过滤条件类型
public enum RuleFilterType {
    // 标签
    LABEL(1, "标签"),
    // 群组
    GROUP(2, "群组"),
    // 事件属性
    ATTR(3, "事件属性")
    ;

    private Integer code;
    private String message;

    RuleFilterType(Integer code, String message) {
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
        for (RuleFilterType value : values()) {
            if (Objects.equals(value.code, code)) {
                return value.message;
            }
        }
        return null;
    }
}