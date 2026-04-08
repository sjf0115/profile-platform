package com.data.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户登录
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
    // Token
    private String token;
    // Token状态：1-有效，0-无效
    private Integer tokenStatus;
    // 登录时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}