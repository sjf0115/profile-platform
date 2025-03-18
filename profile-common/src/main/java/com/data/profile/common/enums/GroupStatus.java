package com.data.profile.common.enums;

// 群组状态
public enum GroupStatus {
    // 未创建
    NOT_CREATE(0, "未创建"),
    // 创建中
    CREATING(1, "创建中"),
    // 创建成功
    CREATE_SUCCESS(2, "创建成功"),
    // 创建失败
    CREATE_FAIL(3, "创建失败"),
    ;

    private Integer code;
    private String message;

    GroupStatus(Integer code, String message) {
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