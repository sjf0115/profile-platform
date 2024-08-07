package com.data.profile.model;

import lombok.Data;

import java.util.Date;

@Data
public class EntityType {
    private Long id;

    private Integer status;

    private String entityTypeId;

    private String entityTypeName;

    private Integer sourceType;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}