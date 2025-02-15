package com.data.profile.model;

import lombok.Data;

import java.util.Date;

@Data
public class Label {
    private Long id;

    private Integer isValid;

    private String labelId;

    private String labelName;

    private Integer labelStatus;

    private String labelType;

    private String labelDesc;

    private String labelCategoryId;

    private Integer labelDataType;

    private Integer labelDistType;

    private Integer labelOrganizeType;

    private Integer labelProduceType;

    private Integer labelTimeType;

    private Integer sourceType;

    private Integer isOffice;

    private String owner;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}