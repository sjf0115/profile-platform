package com.data.profile.web.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

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
    // 实体ID
    private String entityId;
    // 实体对应字段
    private String entityField;
    // 实体字段
    private List<DatasetField> fields;
    // 实例ID
    private String instanceId;
    // 实例状态
    private Integer instanceStatus;
    // 实例开始时间
    private Date instanceStartTime;
    // 实例结束时间
    private Date instanceEndTime;
    // 实例消息
    private Integer instanceMsg;
    // 负责人
    private String owner;
    // 创建人
    private String creator;
    // 修改人
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}