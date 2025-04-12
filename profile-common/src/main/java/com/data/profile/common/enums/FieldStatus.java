package com.data.profile.common.enums;

// 数据集字段状态
public enum FieldStatus {
    // 新增字段
    ADD_FIELD(1, "新增字段"),
    // 修改字段
    UPDATE_FIELD(2, "修改字段"),
    // 删除字段
    DELETE_FIELD(3, "删除字段")
    ;

    private Integer code;
    private String message;

    FieldStatus(Integer code, String message) {
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