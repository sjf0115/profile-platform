package com.data.profile.web.model;

import lombok.Data;

import java.util.Date;

@Data
public class Dataset {
    private Long id;

    private Integer status;
    // 数据集ID
    private String datasetId;
    // 数据集名称
    private String datasetName;
    // 数据集类型
    private Integer datasetType;
    // 数据集描述
    private String datasetDesc;
    // 创建方式
    private Integer sourceType;
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
    // 负责人名称
    private String ownerName;
    // 创建人
    private String creator;
    // 修改人
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}