package com.data.profile.web.dto;

import com.data.profile.web.model.Role;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    // 用户状态
    private Integer status;
    // 用户ID
    private String userId;
    // 用户名称
    private String userName;
    // 用户注册邮箱
    private String email;
    private Integer sourceType;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;
    // 用户角色
    private List<Role> roles;
}