package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户登录历史
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLogin {
    // 自增ID
    private Long id;
    // 用户ID
    private String userId;
    // 登录时间
    private Date loginTime;
    // 登录IP
    private String loginIp;
    // 登录浏览器UserAgent
    private String loginUa;
    // 创建时间
    private Date gmtCreate;
}
