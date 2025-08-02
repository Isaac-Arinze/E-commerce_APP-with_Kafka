package com.sky_ecommerce.auth.api;

public class SignupResponse {
    private String userId;
    private String status;
    private String message;

    public SignupResponse() {
    }

    public SignupResponse(String userId, String status, String message) {
        this.userId = userId;
        this.status = status;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public SignupResponse setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public SignupResponse setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public SignupResponse setMessage(String message) {
        this.message = message;
        return this;
    }
}
