package com.data.profile.common.enums;

public enum ModelType {
    // 用户
    USER("01", "user"),
    // 实体
    ENTITY("02", "entity"),
    // 实体类型
    ENTITY_TYPE("03", "entity_type"),
    // 数据源
    DATASOURCE("04", "datasource"),
    // 数据源类型
    DATASOURCE_TYPE("05", "datasource_type"),
    // 数据集
    DATASET("06", "dataset"),
    // 标签
    LABEL("07", "label"),
    // 标签类目
    LABEL_CATEGORY("08", "label_category"),
    // 群组
    GROUP("09", "group"),
    // 属性
    ATTRIBUTE("10", "attribute"),
    // 事件
    EVENT("11", "event"),
    // 分析
    ANALYSIS("12", "analysis"),
    // 投递
    EXPORT("13", "export"),
    // 质量
    QUALITY("14", "quality"),
    // 调度
    SCHEDULE("15", "schedule"),
    // 任务
    TASK("16", "task"),
    // 工作流
    WORKFLOW("17", "workflow"),
    // 角色
    ROLE("18", "role"),
    // 权限
    PERMISSION("19", "permission")
    ;

    private String code;
    private String message;

    ModelType(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}