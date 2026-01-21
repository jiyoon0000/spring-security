package com.example.jwt.api.dto;

public class TokenIssueRequest {

    private String email;
    private String role;

    public TokenIssueRequest() {
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

}
