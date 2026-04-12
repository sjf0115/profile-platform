package com.data.profile.web.security;

import com.data.profile.web.dto.UserLoginRequest;
import com.data.profile.web.dao.UserMapper;
import com.data.profile.web.model.User;
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
