package com.rice.trade.security;

import com.rice.trade.config.TokenSessionProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class TokenSessionService {

    private final StringRedisTemplate redisTemplate;
    private final TokenSessionProperties properties;

    public TokenSessionService(StringRedisTemplate redisTemplate, TokenSessionProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public void cacheToken(String authorization, Authentication authentication) {
        if (!properties.isEnabled() || authorization == null || authorization.isBlank()) {
            return;
        }

        redisTemplate.opsForValue().set(
                tokenKey(authorization),
                authentication.getName(),
                Duration.ofSeconds(Math.max(1L, properties.getTtlSeconds()))
        );
    }

    public boolean tokenExists(String authorization) {
        if (!properties.isEnabled()) {
            return true;
        }
        if (authorization == null || authorization.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey(authorization)));
    }

    public boolean isLoginRequest(String value) {
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    private String tokenKey(String authorization) {
        return properties.getKeyPrefix() + sha256Hex(authorization);
    }

    private String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
