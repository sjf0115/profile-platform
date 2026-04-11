package com.data.profile.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class User {
    private Long id;
    private Integer status;
    private String userId;
    private String userName;
    private String email;
    // 管理员/普通用户
    private String userType;
    private String password;
    private Integer sourceType;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;
    private List<String> roles;
}