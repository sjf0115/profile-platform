package com.data.profile.common.domain.security;

import com.data.profile.common.domain.Constant;
import com.data.profile.common.domain.request.UserLoginRequest;
import com.data.profile.common.exception.ProfileException;
import com.data.profile.common.utils.PasswordUtil;
import com.data.profile.dao.UserMapper;
import com.data.profile.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;

import static com.data.profile.common.enums.ResponseCode.USERNAME_PASSWORD_NO_MATCHED;

/**
 * 功能：密码验证
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/8 22:54
 */
public class PasswdAuthenticationStrategy implements IAuthenticationStrategy {

    @Autowired
    private UserMapper userMapper;

    @Value("${user.default.passwordSalt:seatunnel}")
    private String defaultSalt;

    // 密码验证
    @Override
    public User authenticate(UserLoginRequest request) {
        final String password = PasswordUtil.encryptWithSalt(defaultSalt, request.getPassword());
        final User user = userMapper.checkPassword(request.getUserName(), password, Constant.AUTHENTICATION_PROVIDER_PASSWORD);
        if (Objects.isNull(user)) {
            throw new ProfileException(USERNAME_PASSWORD_NO_MATCHED);
        }
        return user;
    }
}
