package com.data.profile.common.enums;

// 标签时效性类型
public enum LabelTimeType {
    // 离线标签
    OFFLINE(1, "离线标签"),
    // 实时标签
    REALTIME(2, "实时标签")
    ;

    private Integer code;
    private String message;

    LabelTimeType(Integer code, String message) {
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
