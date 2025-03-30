package com.data.profile.model;

import lombok.Data;

import java.util.Date;

@Data
public class Attribute {
    private Long id;

    private Integer status;

    private String attrId;

    private String attrName;

    private String attrDesc;

    private Integer attrType;

    private String datasetId;

    private String eventId;

    private String entityId;

    private String attrField;

    private String attrPath;

    private Integer attrDataType;

    private Integer attrDistType;

    private Integer attrOrganizeType;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}