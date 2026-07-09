package com.data.profile.web.dto;

import lombok.Data;

/**
 * 功能：数据集查询参数
 * 日期：2026/7/8
 */
@Data
public class DatasetParam {
    // 数据集名称
    private String datasetName;
    // 数据集类型
    private Integer datasetType;
    // 数据集状态
    private Integer status;
    // 实体ID
    private String entityId;
    // 创建方式
    private Integer sourceType;
}
