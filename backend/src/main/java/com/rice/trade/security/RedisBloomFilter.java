package com.rice.trade.security;

import com.rice.trade.config.DdosProtectionProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisBloomFilter {

    private final StringRedisTemplate redisTemplate;
    private final DdosProtectionProperties properties;

    public RedisBloomFilter(StringRedisTemplate redisTemplate, DdosProtectionProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public boolean mightContain(String value) {
        for (long offset : offsets(value)) {
            Boolean exists = redisTemplate.opsForValue().getBit(properties.getBloomKey(), offset);
            if (!Boolean.TRUE.equals(exists)) {
                return false;
            }
        }
        return true;
    }

    public void add(String value) {
        for (long offset : offsets(value)) {
            redisTemplate.opsForValue().setBit(properties.getBloomKey(), offset, true);
        }
    }

    private long[] offsets(String value) {
        int hashCount = Math.max(1, properties.getHashCount());
        long bloomSize = Math.max(1024L, properties.getBloomSize());
        long[] offsets = new long[hashCount];
        for (int index = 0; index < hashCount; index += 1) {
            offsets[index] = positiveLong(sha256(index + ":" + value)) % bloomSize;
        }
        return offsets;
    }

    private long positiveLong(byte[] digest) {
        return ByteBuffer.wrap(digest, 0, Long.BYTES).getLong() & Long.MAX_VALUE;
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
