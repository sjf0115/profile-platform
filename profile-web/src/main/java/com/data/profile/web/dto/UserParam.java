package com.data.profile.web.dto;

import lombok.Data;

/**
 * 功能：用户查询参数
 * 日期：2026/7/8
 */
@Data
public class UserParam {
    // 用户名称
    private String userName;
    // 用户注册邮箱
    private String email;
    // 用户状态
    private Integer status;
    // 创建方式
    private Integer sourceType;
}
