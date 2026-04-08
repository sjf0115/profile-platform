package com.data.profile.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil implements InitializingBean {
    @Value("${jwt.expireTime}")
    private int expireTime;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.algorithm}")
    private String algorithmString;

    private SignatureAlgorithm algorithm = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        algorithm = SignatureAlgorithm.valueOf(algorithmString);
    }

    public String genToken(Map<String, Object> data) {
        final Date currentDate = new Date();
        final Date expireDate = DateUtils.addSeconds(currentDate, expireTime);

        return Jwts.builder()
                .signWith(algorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                .setId(UUID.randomUUID().toString())
                .setClaims(data)
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .compact();
    }

    public Map<String, Object> parseToken(String token) {
        final Jws<Claims> claims = Jwts.parser()
                        .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                        .parseClaimsJws(token);
        return claims.getBody();
    }
}
