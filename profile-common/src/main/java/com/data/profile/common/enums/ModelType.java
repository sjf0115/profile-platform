package com.data.profile.common.enums;

public enum ModelType {
    // 用户
    USER("01", "用户"),
    // 实体标识
    ENTITY_IDENTIFIER("02", "实体标识"),
    // 实体
    ENTITY("03", "实体"),
    // 数据源Schema
    DATASOURCE_SCHEMA("04", "数据源Schema"),
    // 数据源
    DATASOURCE("05", "数据源"),
    // 数据集
    DATASET("06", "数据集"),
    // 标签类目
    LABEL_CATEGORY("07", "标签类目"),
    // 标签
    LABEL("08", "标签"),
    // 群组
    GROUP("09", "群组"),
    // 投递
    EXPORT("10", "投递"),
    // 属性
    ATTRIBUTE("11", "属性"),
    // 事件
    EVENT("12", "事件"),
    // 分析
    ANALYSIS("13", "分析"),
    // 质量
    QUALITY("14", "质量"),
    // 任务
    TASK("15", "任务"),
    // 实例
    INSTANCE("16", "实例"),
    // 工作流
    WORKFLOW("17", "工作流"),
    // 角色
    ROLE("18", "角色"),
    // 权限
    PERMISSION("19", "权限")
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