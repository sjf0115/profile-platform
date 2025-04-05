package com.data.profile.model;

import lombok.Data;

import java.util.Date;

@Data
public class Entity {
    private Integer id;

    private Integer status;

    private String entityId;

    private String entityName;

    private String entityTypeId;

    private String entityTypeName;

    private Integer sourceType;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}