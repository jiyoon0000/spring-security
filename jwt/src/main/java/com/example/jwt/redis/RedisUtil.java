package com.example.jwt.redis;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {

    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";
    private static final String BLACKLIST_VALUE = "1";

    private final StringRedisTemplate redisTemplate;

    public RedisUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long ttlMillis) {
        if (ttlMillis <= 0) {
            return;
        }
        redisTemplate.opsForValue()
            .set(TOKEN_BLACKLIST_PREFIX + token, BLACKLIST_VALUE, Duration.ofMillis(ttlMillis));
    }

    public boolean isBlacklisted(String token) {
        String value = redisTemplate.opsForValue().get(TOKEN_BLACKLIST_PREFIX + token);
        return value != null;
    }

}
