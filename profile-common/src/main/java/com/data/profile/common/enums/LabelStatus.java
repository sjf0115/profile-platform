package com.data.profile.common.enums;

// 标签状态（基于数据集绑定约束）
public enum LabelStatus {
    // 未绑定：已创建但未绑定数据集字段，不可用
    UNBOUND(0, "未绑定"),
    // 已启用：已绑定数据集字段，正常使用
    ENABLED(1, "已启用"),
    // 已停用：已绑定但手动停用，暂停使用
    DISABLED(2, "已停用");

    private Integer code;
    private String message;

    LabelStatus(Integer code, String message) {
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