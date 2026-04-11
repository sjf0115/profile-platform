package com.data.profile.web.security;

import com.data.profile.common.domain.request.UserLoginRequest;
import com.data.profile.web.model.User;

/**
 * 功能：AuthenticationStrategy 接口
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/8 22:52
 */
public interface IAuthenticationStrategy {
    User authenticate(UserLoginRequest request);
}