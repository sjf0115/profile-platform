package com.data.profile.web.dto;

import lombok.Data;

import java.util.List;

/**
 * 功能：用户请求DTO
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Data
public class UserRequest {
    private String userName;
    private String email;
    private String password;
    /**
     * 角色ID列表
     */
    private List<String> roles;
}
