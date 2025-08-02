package com.sky_ecommerce.auth.api;

public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public LoginResponse setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getTokenType() {
        return tokenType;
    }

    public LoginResponse setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public LoginResponse setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }
}
