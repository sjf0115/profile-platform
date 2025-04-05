package com.data.profile.model;

import lombok.Data;

import java.util.Date;

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

    private String sourceTableName;

    private String sourcePartitionField;

    private String sourcePartitionFormat;

    private String sinkTableName;

    private String entityId;

    private String entityField;

    private Integer syncStatus;

    private Date syncStartTime;

    private Date syncEndTime;

    private Integer syncTime;

    private String owner;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}