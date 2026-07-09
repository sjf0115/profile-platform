package com.data.profile.web.dto;

import lombok.Data;

/**
 * 功能：可分析标签聚合传输对象（Service 层出参）
 * 三方 JOIN: Label + DatasetField + Dataset
 */
@Data
public class AnalysisLabelDTO {
    // 标签ID
    private String labelId;
    // 标签名称
    private String labelName;
    // 标签类目ID
    private String labelCategoryId;
    // 标签类目名称
    private String labelCategoryName;
    // 标签数据类型: 1-文本型,2-数值型,3-时间型
    private Integer labelDataType;
    // 数据集ID
    private String datasetId;
    // 数据集名称
    private String datasetName;
    // 数据集字段名称
    private String fieldName;
    // 更新方式: 1-手动, 2-周期
    private Integer updateType;
}
