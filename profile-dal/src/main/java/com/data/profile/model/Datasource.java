package com.data.profile.model;

import lombok.Data;

import java.util.Date;

@Data
public class Datasource {
    private Long id;

    private Integer status;

    private String datasourceId;

    private String datasourceName;

    private String datasourceDesc;

    private String datasourceTypeId;

    private String datasourceTypeName;

    private Integer sourceType;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;

    private String config;

    private String configTemplate;
}