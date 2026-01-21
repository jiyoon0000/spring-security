package com.example.jwt.security;

import com.example.jwt.error.ErrorCode;
import com.example.jwt.error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doFilterInternal(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String token = jwtProvider.resolveToken(httpServletRequest.getHeader(JwtProvider.AUTH_HEADER));

        try {
            if (token != null) {
                jwtProvider.validateTokenOrThrow(token);

                String email = jwtProvider.getEmailFromToken(token);
                String role = jwtProvider.getRoleFromToken(token);

                JwtMemberDetails memberDetails = new JwtMemberDetails(email, role);

                UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
            setErrorResponse(httpServletResponse, httpServletRequest, ErrorCode.TOKEN_EXPIRED);
            return;

        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token");
            setErrorResponse(httpServletResponse, httpServletRequest, ErrorCode.TOKEN_MALFORMED);
            return;

        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token");
            setErrorResponse(httpServletResponse, httpServletRequest, ErrorCode.TOKEN_UNSUPPORTED);
            return;

        } catch (SecurityException e) {
            log.warn("Invalid JWT signature");
            setErrorResponse(httpServletResponse, httpServletRequest, ErrorCode.TOKEN_SIGNATURE_INVALID);
            return;

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token");
            setErrorResponse(httpServletResponse, httpServletRequest, ErrorCode.TOKEN_ILLEGAL);
            return;

        } catch (Exception e) {
            log.error("JWT authentication error" , e);
            setErrorResponse(httpServletResponse, httpServletRequest, ErrorCode.INTERNAL_SERVER_ERROR);
            return;
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void setErrorResponse(
        HttpServletResponse httpServletResponse,
        HttpServletRequest httpServletRequest,
        ErrorCode errorCode
    ) throws IOException {

        httpServletResponse.setStatus(errorCode.getHttpStatus().value());
        httpServletResponse.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(errorCode, httpServletRequest.getRequestURI(), httpServletRequest.getMethod());

        httpServletResponse.getWriter()
            .write(objectMapper.writeValueAsString(errorResponse));

        httpServletResponse.flushBuffer();
    }

}
