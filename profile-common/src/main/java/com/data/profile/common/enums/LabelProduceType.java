package com.data.profile.common.enums;

// 标签生产类型
public enum LabelProduceType {
    // 事实标签
    FACT(1, "事实标签"),
    // 统计标签
    STATS(2, "统计标签"),
    // 预测标签
    PREDICT(3, "算法标签")
    ;

    private Integer code;
    private String message;

    LabelProduceType(Integer code, String message) {
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