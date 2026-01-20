package com.example.jwt.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtProvider {

    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final SecretKey key;

    @Getter
    private final long accessTokenExpiry;

    @Getter
    private final long refreshTokenExpiry;

    public JwtProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiry-millis}") long accessTokenExpiry,
        @Value("${jwt.refresh-expiry-millis}") long refreshTokenExpiry
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    public String generateAccessToken(String email, String role) {
        return generateToken(email, role, accessTokenExpiry);
    }

    public String generateRefreshToken(String email, String role) {
        return generateToken(email, role, refreshTokenExpiry);
    }

    private String generateToken(String email, String role, long expiryMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMillis);

        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getPayload().getSubject();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).getPayload().get("role", String.class);
    }

    public long getExpirationMillis(String token) {
        Date expiration = getClaims(token).getPayload().getExpiration();

        return expiration.getTime() - System.currentTimeMillis();
    }

    public void validateTokenOrThrow(String token) throws JwtException {
        getClaims(token);
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token);
    }

}
