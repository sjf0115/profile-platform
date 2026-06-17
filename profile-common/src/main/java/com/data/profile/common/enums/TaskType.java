package com.data.profile.common.enums;

import lombok.Getter;

/**
 * 任务类型枚举
 * <p>统一的任务类型定义，用于任务创建、执行分发等场景。</p>
 */
@Getter
public enum TaskType {
    // 1-群组圈选,2-群组投递,3-数据集同步
    /**
     * 群组圈选
     */
    GROUP(1, "GroupJob"),
    /**
     * 群组投递
     */
    EXPORT(2, "ExportJob"),
    /**
     * 数据集同步
     */
    IMPORT(3, "ImportJob");

    private final Integer code;
    private final String name;

    TaskType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据 code 获取枚举值
     */
    public static TaskType of(Integer code) {
        if (code == null) return null;
        for (TaskType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的任务类型: " + code);
    }
}
