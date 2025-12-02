package com.data.profile.common.enums;

public enum ModelType {
    // 用户
    USER("01", "用户"),
    // 实体
    ENTITY("02", "实体"),
    // 实体类型
    ENTITY_TYPE("03", "实体类型"),
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
    // 属性
    ATTRIBUTE("10", "属性"),
    // 事件
    EVENT("11", "事件"),
    // 分析
    ANALYSIS("12", "分析"),
    // 投递
    EXPORT("13", "投递"),
    // 质量
    QUALITY("14", "质量"),
    // 任务
    TASK("15", "任务"),
    // 任务实例
    INSTANCE("16", "任务实例"),
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