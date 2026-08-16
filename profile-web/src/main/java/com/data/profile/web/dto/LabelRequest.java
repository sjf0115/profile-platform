package com.data.profile.web.dto;

import com.data.profile.web.model.LabelConfig;
import lombok.Data;

/**
 * 标签创建/更新请求体
 * 注：isValid/labelStatus/creator/modifier/gmtCreate/gmtModified 由后端自动管理，不接受前端传入
 */
@Data
public class LabelRequest {
    // 标签名称
    private String labelName;
    // 标签描述
    private String labelDesc;
    // 标签类型: 1-属性标签,2-行为标签
    private String labelType;
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
    // 创建方式: 1-系统内置,2-数据集导入,3-文件上传,4-四则运算,5-SQL计算,6-自定义规则,7-API导入,8-数据表导入
    private Integer sourceType;
    // 标签计算规则（文件上传场景携带 physical_path/origin_path）
    private LabelConfig config;
    // 是否官方认证:0-否,1-是
    private Integer isOffice;
    // 标签负责人
    private String owner;
    //----------------------------------------------------------
    // 关联写入字段
    // 绑定的数据集ID
    private String datasetId;
    // 绑定的数据集字段名称
    private String datasetFieldName;
}