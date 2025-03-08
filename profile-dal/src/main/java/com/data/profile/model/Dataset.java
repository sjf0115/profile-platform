package com.data.profile.model;

import java.util.Date;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId == null ? null : datasetId.trim();
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName == null ? null : datasetName.trim();
    }

    public Integer getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(Integer datasetType) {
        this.datasetType = datasetType;
    }

    public String getDatasetDesc() {
        return datasetDesc;
    }

    public void setDatasetDesc(String datasetDesc) {
        this.datasetDesc = datasetDesc == null ? null : datasetDesc.trim();
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public String getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(String datasourceId) {
        this.datasourceId = datasourceId == null ? null : datasourceId.trim();
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName == null ? null : sourceTableName.trim();
    }

    public String getSourcePartitionField() {
        return sourcePartitionField;
    }

    public void setSourcePartitionField(String sourcePartitionField) {
        this.sourcePartitionField = sourcePartitionField == null ? null : sourcePartitionField.trim();
    }

    public String getSourcePartitionFormat() {
        return sourcePartitionFormat;
    }

    public void setSourcePartitionFormat(String sourcePartitionFormat) {
        this.sourcePartitionFormat = sourcePartitionFormat == null ? null : sourcePartitionFormat.trim();
    }

    public String getSinkTableName() {
        return sinkTableName;
    }

    public void setSinkTableName(String sinkTableName) {
        this.sinkTableName = sinkTableName == null ? null : sinkTableName.trim();
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    public String getEntityField() {
        return entityField;
    }

    public void setEntityField(String entityField) {
        this.entityField = entityField == null ? null : entityField.trim();
    }

    public Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Date getSyncStartTime() {
        return syncStartTime;
    }

    public void setSyncStartTime(Date syncStartTime) {
        this.syncStartTime = syncStartTime;
    }

    public Date getSyncEndTime() {
        return syncEndTime;
    }

    public void setSyncEndTime(Date syncEndTime) {
        this.syncEndTime = syncEndTime;
    }

    public Integer getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(Integer syncTime) {
        this.syncTime = syncTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner == null ? null : owner.trim();
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator == null ? null : creator.trim();
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier == null ? null : modifier.trim();
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }
}