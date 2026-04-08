package com.data.profile.common.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：用户登录请求
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/8 22:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {
    private String userName;
    private String password;
}
