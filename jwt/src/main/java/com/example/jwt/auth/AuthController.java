package com.example.jwt.auth;

import com.example.jwt.redis.RedisUtil;
import com.example.jwt.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtProvider jwtProvider;
    private final RedisUtil redisUtil;

    public AuthController(JwtProvider jwtProvider, RedisUtil redisUtil) {
        this.jwtProvider = jwtProvider;
        this.redisUtil = redisUtil;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String bearer = request.getHeader(JwtProvider.AUTH_HEADER);
        String token = jwtProvider.resolveToken(bearer);

        if (token != null) {
            long ttlMillis = jwtProvider.getExpirationMillis(token);
            redisUtil.blacklistToken(token, ttlMillis);
        }

        return ResponseEntity.noContent().build();
    }

}
