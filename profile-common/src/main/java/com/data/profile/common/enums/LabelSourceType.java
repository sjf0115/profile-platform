package com.data.profile.common.enums;

// 创建方式
public enum LabelSourceType {
    // 系统内置
    BUILT_IN(1, "系统内置"),
    // 数据集导入
    DATASET(2, "数据集导入"),
    // 文件上传
    FILE(3, "文件上传"),
    // 四则运算
    OPERATIONS(4, "四则运算"),
    // SQL计算
    SQL(5, "SQL计算"),
    // 自定义规则
    CUSTOM(6, "自定义规则"),
    // API导入
    API(7, "API导入"),
    // 数据表导入
    TABLE(8, "数据表导入")
    ;

    private Integer code;
    private String message;

    LabelSourceType(Integer code, String message) {
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
