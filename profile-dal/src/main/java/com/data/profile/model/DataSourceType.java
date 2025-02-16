package com.data.profile.model;

import java.util.Date;

public class DataSourceType {
    private Long id;

    private Integer status;

    private String dataSourceTypeId;

    private String dataSourceTypeName;

    private Integer sourceType;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;

    private String configTemplate;

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

    public String getDataSourceTypeId() {
        return dataSourceTypeId;
    }

    public void setDataSourceTypeId(String dataSourceTypeId) {
        this.dataSourceTypeId = dataSourceTypeId == null ? null : dataSourceTypeId.trim();
    }

    public String getDataSourceTypeName() {
        return dataSourceTypeName;
    }

    public void setDataSourceTypeName(String dataSourceTypeName) {
        this.dataSourceTypeName = dataSourceTypeName == null ? null : dataSourceTypeName.trim();
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
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

    public String getConfigTemplate() {
        return configTemplate;
    }

    public void setConfigTemplate(String configTemplate) {
        this.configTemplate = configTemplate == null ? null : configTemplate.trim();
    }
}