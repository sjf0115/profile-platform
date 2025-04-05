package com.data.profile.common.enums;

// 数据源Schema类型
public enum DatasourceSchemaType {
    // source
    SOURCE(1, "source"),
    // sink
    SINK(2, "sink"),
    // source/sink
    BOTH(3, "both")
    ;

    private Integer code;
    private String message;

    DatasourceSchemaType(Integer code, String message) {
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