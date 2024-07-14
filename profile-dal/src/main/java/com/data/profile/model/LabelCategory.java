package com.data.profile.model;

import lombok.Data;

import java.util.Date;

@Data
public class LabelCategory {
    private Long id;

    private Integer status;

    private Integer isDefault;

    private String categoryId;

    private String categoryName;

    private Integer categoryLevel;

    private String parentCategoryId;

    private Integer categorySeq;

    private Integer sourceType;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}