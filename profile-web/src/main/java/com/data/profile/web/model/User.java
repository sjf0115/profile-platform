package com.data.profile.web.model;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Long id;
    // 用户状态
    private Integer status;
    // 用户ID
    private String userId;
    // 用户名称
    private String userName;
    // 用户注册邮箱
    private String email;
    // 用户密码
    private String password;
    private Integer sourceType;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;
}