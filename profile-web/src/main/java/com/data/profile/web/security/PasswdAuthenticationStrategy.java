package com.data.profile.web.security;

import com.data.profile.web.dto.UserLoginRequest;
import com.data.profile.common.domain.Constant;
import com.data.profile.common.exception.ProfileException;
import com.data.profile.web.utils.PasswordUtil;
import com.data.profile.web.dao.UserMapper;
import com.data.profile.web.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.data.profile.common.enums.ResponseCode.USERNAME_PASSWORD_NO_MATCHED;

/**
 * 功能：密码验证
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/8 22:54
 */
@Component
public class PasswdAuthenticationStrategy implements IAuthenticationStrategy {

    @Autowired
    private UserMapper userMapper;

    @Value("${user.default.passwordSalt:profile}")
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
