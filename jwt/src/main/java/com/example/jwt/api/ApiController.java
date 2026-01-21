package com.example.jwt.api;

import com.example.jwt.api.dto.TokenIssueRequest;
import com.example.jwt.api.dto.TokenIssueResponse;
import com.example.jwt.redis.RedisUtil;
import com.example.jwt.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ApiController {

    private final JwtProvider jwtProvider;
    private final RedisUtil redisUtil;

    public ApiController(JwtProvider jwtProvider, RedisUtil redisUtil) {
        this.jwtProvider = jwtProvider;
        this.redisUtil = redisUtil;
    }

    // permitAll
    @GetMapping("/public/ping")
    public ResponseEntity<String> publicPing() {
        return ResponseEntity.ok("public pong");
    }

    // authenticated
    @GetMapping("/private/ping")
    public ResponseEntity<String> privatePing() {
        return ResponseEntity.ok("private pong");
    }

    // permitAll - test Token
    @PostMapping("/auth/token")
    public ResponseEntity<TokenIssueResponse> issueToken(@RequestBody TokenIssueRequest request) {
        String accessToken = jwtProvider.generateAccessToken(request.getEmail(), request.getRole());
        return ResponseEntity.ok(new TokenIssueResponse(accessToken));
    }

    // authenticated - logout, blacklist
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
