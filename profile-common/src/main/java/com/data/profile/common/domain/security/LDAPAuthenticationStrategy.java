package com.data.profile.common.domain.security;

import com.data.profile.common.domain.request.UserLoginRequest;
import com.data.profile.dao.UserMapper;
import com.data.profile.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LDAPAuthenticationStrategy implements IAuthenticationStrategy {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User authenticate(UserLoginRequest request) {
        return null;
    }
}
