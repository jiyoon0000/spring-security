package com.example.jwt.api.dto;

public class TokenIssueResponse {

    private final String accessToken;

    public TokenIssueResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

}
