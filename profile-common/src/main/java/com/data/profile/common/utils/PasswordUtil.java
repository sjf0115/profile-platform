package com.data.profile.common.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class PasswordUtil {
    // 加密
    public static String encryptWithSalt(String salt, String password) {
        return DigestUtils.md5DigestAsHex(salt.concat(password).getBytes(StandardCharsets.UTF_8));
    }
}
