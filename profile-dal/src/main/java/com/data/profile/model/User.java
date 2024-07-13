package com.data.profile.model;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Long id;

    private Integer status;

    private String userId;

    private String userName;

    private Integer sourceType;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}