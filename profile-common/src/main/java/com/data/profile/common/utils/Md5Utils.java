package com.data.profile.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Md5Utils
 */
public class Md5Utils {

    private static final Logger logger = LoggerFactory.getLogger(Md5Utils.class);

    private Md5Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getMd5(String src, boolean isUpper) {
        String md5 = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            Base64.Encoder encoder = Base64.getEncoder();
            md5 = encoder.encodeToString(md.digest(src.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            logger.error("get md5 error: {}", e.getMessage());
        }

        if (isUpper) {
            md5 = md5.toUpperCase();
        } else {
            md5 = md5.toLowerCase();
        }

        return md5;
    }
}
