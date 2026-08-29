package com.data.profile.common.enums;

import lombok.Getter;

/**
 * 投递方式
 */
@Getter
public enum ExportMode {
    // 数据源投递
    DATASOURCE(1, "数据源"),
    // 应用投递
    APPLICATION(2, "应用")
    ;

    private final Integer code;
    private final String description;

    ExportMode(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ExportMode of(Integer code) {
        if (code == null) return null;
        for (ExportMode mode : values()) {
            if (mode.code.equals(code)) return mode;
        }
        throw new IllegalArgumentException("未知的投递方式: " + code);
    }
}
