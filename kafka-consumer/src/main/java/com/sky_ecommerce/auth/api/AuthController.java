package com.sky_ecommerce.auth.api;

import com.sky_ecommerce.auth.service.AuthService;
import com.sky_ecommerce.auth.api.ForgotPasswordRequest;
import com.sky_ecommerce.auth.api.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping(path = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return service.signup(request);
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);
        return service.login(request, clientIp);
    }

    @PostMapping(path = "/verify-otp", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        boolean ok = service.verifyOtp(request);
        return Map.of("status", ok ? "VERIFIED" : "INVALID_OR_EXPIRED");
    }

    @PostMapping(path = "/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        service.requestPasswordReset(request.getEmail());
        return Map.of("status", "OK");
    }

    @PostMapping(path = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean ok = service.resetPassword(request.getToken(), request.getNewPassword());
        return ok ? Map.of("status", "OK") : Map.of("status", "INVALID_OR_EXPIRED");
    }

    @GetMapping(path = "/profile/{userId}")
    public UserDto profile(@PathVariable String userId) {
        return service.getProfile(userId).orElse(null);
    }

    private String extractClientIp(HttpServletRequest request) {
        String h = request.getHeader("X-Forwarded-For");
        if (h != null && !h.isBlank()) {
            int idx = h.indexOf(',');
            return idx > 0 ? h.substring(0, idx).trim() : h.trim();
        }
        return request.getRemoteAddr();
    }
}
