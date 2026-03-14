package com.data.profile.common.enums;

import java.util.Objects;

// 数据源Schema分类
public enum DataSourceSchemaType {
    // 数据库
    DATABASE(1, "数据库"),
    // 分析性数据库
    OLAP(2, "分析性数据库"),
    // 对象存储
    OSS(3, "对象存储"),
    // 消息队列
    MQ(4, "消息队列")
    ;

    private Integer code;
    private String message;

    DataSourceSchemaType(Integer code, String message) {
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
        for (DataSourceSchemaType value : values()) {
            if (Objects.equals(value.code, code)) {
                return value.message;
            }
        }
        return null;
    }
}