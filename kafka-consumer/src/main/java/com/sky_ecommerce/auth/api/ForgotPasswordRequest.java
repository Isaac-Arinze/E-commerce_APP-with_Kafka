package com.sky_ecommerce.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {

    @NotBlank
    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public ForgotPasswordRequest setEmail(String email) {
        this.email = email;
        return this;
    }
}
