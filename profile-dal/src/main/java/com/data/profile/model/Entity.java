package com.data.profile.model;

import lombok.Data;

import java.util.Date;

@Data
public class Entity {
    private Long id;

    private Integer status;

    private String entityId;

    private String entityName;

    private Integer sourceType;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}