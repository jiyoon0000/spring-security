package com.example.jwt.security.handler;

import com.example.jwt.error.ErrorCode;
import com.example.jwt.error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class DelegatedAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public DelegatedAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {

        writeError(response, request, ErrorCode.TOKEN_ILLEGAL);
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request, ErrorCode errorCode)
        throws IOException {

        response.setStatus(errorCode.getHttpStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse body = ErrorResponse.of(errorCode, request.getRequestURI(), request.getMethod());
        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.getWriter().flush();
    }

}
