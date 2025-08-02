package com.sky_ecommerce.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyOtpRequest {

    @NotBlank
    private String userId;

    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String code;

    public VerifyOtpRequest() {
    }

    public String getUserId() {
        return userId;
    }

    public VerifyOtpRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getCode() {
        return code;
    }

    public VerifyOtpRequest setCode(String code) {
        this.code = code;
        return this;
    }
}
