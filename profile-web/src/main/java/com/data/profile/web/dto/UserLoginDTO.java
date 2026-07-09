package com.data.profile.web.dto;

import com.data.profile.web.model.Role;
import com.data.profile.web.model.User;
import lombok.Data;

import java.util.List;

/**
 * 用户登录聚合结果（Service 层出参）
 * 包含用户信息、Token、角色列表
 */
@Data
public class UserLoginDTO {
    private User user;
    private String token;
    private List<Role> roles;
}
