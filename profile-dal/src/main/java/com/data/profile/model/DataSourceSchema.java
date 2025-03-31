package com.data.profile.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DataSourceSchema {
    private Long id;

    private Integer status;

    private String schemaId;

    private String schemaName;

    private String schemaType;

    private Integer sourceType;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;

    private List<SchemaConfigItem> configTemplate;
}