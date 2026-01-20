package com.example.jwt.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {

    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public RedisUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getBlacklistedToken(String token) {
        return redisTemplate.opsForValue().get(TOKEN_BLACKLIST_PREFIX + token);
    }

}
