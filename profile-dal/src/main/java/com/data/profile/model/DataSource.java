package com.data.profile.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DataSource {
    private Long id;

    private Integer status;

    private String datasourceId;

    private String datasourceName;

    private String datasourceDesc;

    private String schemaId;

    private String schemaName;

    private String schemaType;

    private Integer sourceType;

    private String owner;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;

    private String config;

    private List<SchemaConfigItem> configTemplate;
}