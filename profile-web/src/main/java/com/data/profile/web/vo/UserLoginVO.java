package com.data.profile.web.vo;

import lombok.Data;

/**
 * 用户登录响应对象（包含用户信息和 Token）
 */
@Data
public class UserLoginVO {
    private UserVO user;
    private String token;
}
