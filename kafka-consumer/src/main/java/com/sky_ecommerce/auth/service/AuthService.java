package com.sky_ecommerce.auth.service;

import com.sky_ecommerce.auth.api.SignupRequest;
import com.sky_ecommerce.auth.api.SignupResponse;
import com.sky_ecommerce.auth.api.UserDto;
import com.sky_ecommerce.auth.api.VerifyOtpRequest;
import com.sky_ecommerce.auth.domain.OtpToken;
import com.sky_ecommerce.auth.domain.OtpTokenRepository;
import com.sky_ecommerce.auth.domain.User;
import com.sky_ecommerce.auth.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final Random random = new Random();

    public AuthService(UserRepository userRepository, OtpTokenRepository otpTokenRepository) {
        this.userRepository = userRepository;
        this.otpTokenRepository = otpTokenRepository;
    }

    @Transactional
    public SignupResponse signup(SignupRequest req) {
        Optional<User> existing = userRepository.findByEmail(req.getEmail().toLowerCase());
        if (existing.isPresent()) {
            return new SignupResponse(null, "ALREADY_EXISTS", "Email already registered");
        }

        User user = new User()
                .setEmail(req.getEmail().toLowerCase())
                // NOTE: just store raw for skeleton; real impl should hash
                .setPasswordHash(req.getPassword())
                .setFirstName(req.getFirstName())
                .setLastName(req.getLastName());

        user = userRepository.save(user);

        // create OTP
        String code = String.format("%06d", random.nextInt(1_000_000));
        OtpToken token = new OtpToken()
                .setUserId(user.getId())
                .setCode(code)
                .setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .setUsed(false);
        otpTokenRepository.save(token);

        return new SignupResponse(user.getId(), "PENDING", "Signup successful. Verify OTP sent.");
    }

    @Transactional
    public boolean verifyOtp(VerifyOtpRequest req) {
        Instant now = Instant.now();
        return otpTokenRepository
                .findFirstByUserIdAndCodeAndUsedFalseAndExpiresAtAfter(req.getUserId(), req.getCode(), now)
                .map(token -> {
                    token.setUsed(true);
                    otpTokenRepository.save(token);
                    userRepository.findById(req.getUserId()).ifPresent(u -> u.setStatus(User.UserStatus.ACTIVE));
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> getProfile(String userId) {
        return userRepository.findById(userId).map(u ->
                new UserDto()
                        .setId(u.getId())
                        .setEmail(u.getEmail())
                        .setStatus(u.getStatus() != null ? u.getStatus().name() : null)
                        .setFirstName(u.getFirstName())
                        .setLastName(u.getLastName())
                        .setCreatedAt(u.getCreatedAt())
                        .setUpdatedAt(u.getUpdatedAt())
        );
    }
}
