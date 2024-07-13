package com.data.profile.model;

import lombok.Data;

import java.util.Date;

@Data
public class DatasourceType {
    private Long id;

    private Integer status;

    private String datasourceTypeId;

    private String datasourceTypeName;

    private Integer sourceType;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;

    private String configTemplate;
}