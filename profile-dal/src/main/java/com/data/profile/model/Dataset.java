package com.data.profile.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Dataset {
    private Long id;

    private Integer status;

    private String datasetId;

    private String datasetName;

    private Integer datasetType;

    private String datasetDesc;

    private Integer sourceType;

    private String datasourceId;

    private String tableName;

    private String partitionField;

    private String partitionFormat;

    private String entityId;

    private String entityField;

    private List<DatasetField> fields;

    private String instanceId;

    private Integer instanceStatus;

    private Date instanceStartTime;

    private Date instanceEndTime;

    private Integer instanceMsg;

    private String owner;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}