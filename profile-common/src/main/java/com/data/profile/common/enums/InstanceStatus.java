package com.data.profile.common.enums;

// 任务实例状态
public enum InstanceStatus {
    // 未运行
    PENDING(1, "pending"),
    // 运行中
    RUNNING(2, "running"),
    // 运行失败
    FAILED(3, "failed"),
    // 运行成功
    SUCCESS(4, "success");

    private Integer code;
    private String message;

    InstanceStatus(Integer code, String message) {
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