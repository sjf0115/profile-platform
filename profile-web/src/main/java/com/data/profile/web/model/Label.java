package com.data.profile.web.model;

import lombok.Data;

import java.util.Date;

@Data
public class Label {
    private Long id;
    // 是否有效
    private Integer isValid;
    // 标签ID
    private String labelId;
    // 标签名称
    private String labelName;
    // 标签状态
    private Integer labelStatus;
    // 标签类型: 1-属性标签,2-行为标签
    private String labelType;
    // 标签描述
    private String labelDesc;
    // 标签类目ID
    private String labelCategoryId;
    // 标签数据类型: 1-文本型,2-数值型,3-时间型
    private Integer labelDataType;
    // 标签数据分布类型: 1-枚举,2-非枚举
    private Integer labelDistType;
    // 标签组织类型: 1-单值,2-多值,3-KV,4-KKV
    private Integer labelOrganizeType;
    // 标签加工类型: 0-未知,1-事实标签,2-统计标签,3-预测标签
    private Integer labelProduceType;
    // 标签时效性类型: 0-未知,1-离线标签,2-实时标签
    private Integer labelTimeType;
    // 标签实体标识ID
    private String entityIdentifierId;
    // 标签实体标识名称
    private String entityIdentifierName;
    // 标签实体ID
    private String entityId;
    // 标签标识名称
    private String entityName;
    // 创建方式: 1-系统内置,2-数据源导入,3-文件上传,4-四则运算,5-SQL计算,6-自定义规则,7-API导入,8-数据表导入
    private Integer sourceType;
    // 标签计算规则,不同创建方式不同规则
    private LabelConfig config;
    // 是否官方认证:0-否,1-是
    private Integer isOffice;
    // 标签负责人
    private String owner;
    // 标签创建者
    private String creator;
    // 标签最后修改者
    private String modifier;
    // 标签创建时间
    private Date gmtCreate;
    // 标签最后修改时间
    private Date gmtModified;
    // 数据集ID
    private String datasetId;
    // 数据集字段名称
    private String datasetFieldName;
}