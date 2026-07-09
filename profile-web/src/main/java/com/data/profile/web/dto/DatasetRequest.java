package com.data.profile.web.dto;

import com.data.profile.web.model.DatasetField;
import lombok.Data;

import java.util.List;

/**
 * 功能：数据集保存请求
 * 日期：2026/7/8
 */
@Data
public class DatasetRequest {
    // 数据集ID（修改时传入）
    private String datasetId;
    // 数据集名称
    private String datasetName;
    // 数据集类型
    private Integer datasetType;
    // 数据集描述
    private String datasetDesc;
    // 数据源ID
    private String datasourceId;
    // 表名
    private String tableName;
    // 分区字段
    private String partitionField;
    // 分区格式
    private String partitionFormat;
    // 实体ID(实体标识ID)
    private String entityId;
    // 实体对应字段
    private String entityField;
    // 负责人
    private String owner;
    // 数据集字段列表
    private List<DatasetField> fields;
}
