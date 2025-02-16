package com.data.profile.model;

import lombok.Data;

import java.util.Date;

@Data
public class DataSource {
    private Long id;

    private Integer status;

    private String dataSourceId;

    private String dataSourceName;

    private String dataSourceDesc;

    private String dataSourceTypeId;

    private String dataSourceTypeName;

    private Integer sourceType;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;

    private String config;

    private String configTemplate;
}