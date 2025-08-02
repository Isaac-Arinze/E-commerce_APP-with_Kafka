package com.sky_ecommerce.auth.api;

import com.sky_ecommerce.auth.service.AuthService;
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

    @PostMapping(path = "/verify-otp", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        boolean ok = service.verifyOtp(request);
        return Map.of("status", ok ? "VERIFIED" : "INVALID_OR_EXPIRED");
    }

    @GetMapping(path = "/profile/{userId}")
    public UserDto profile(@PathVariable String userId) {
        return service.getProfile(userId).orElse(null);
    }
}
