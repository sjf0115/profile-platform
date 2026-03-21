package com.data.profile.common.enums;

import java.util.Objects;

// 调度任务类型
public enum TaskType {
    // 数据集
    DATASET(1, "数据集"),
    // 群组圈选
    GROUP_CREATE(2, "群组圈选")
    ;

    private Integer code;
    private String message;

    TaskType(Integer code, String message) {
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
        for (TaskType value : values()) {
            if (Objects.equals(value.code, code)) {
                return value.message;
            }
        }
        return null;
    }
}