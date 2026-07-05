package com.data.profile.web.vo;

import lombok.Data;

/**
 * 功能：可分析标签 VO
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Data
public class AnalysisLabelVO {
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
