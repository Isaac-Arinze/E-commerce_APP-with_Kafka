package com.sky_ecommerce.auth.service;

import com.sky_ecommerce.auth.api.LoginRequest;
import com.sky_ecommerce.auth.api.LoginResponse;
import com.sky_ecommerce.auth.api.SignupRequest;
import com.sky_ecommerce.auth.api.SignupResponse;
import com.sky_ecommerce.auth.api.UserDto;
import com.sky_ecommerce.auth.api.VerifyOtpRequest;
import com.sky_ecommerce.auth.domain.OtpToken;
import com.sky_ecommerce.auth.domain.OtpTokenRepository;
import com.sky_ecommerce.auth.domain.PasswordResetToken;
import com.sky_ecommerce.auth.domain.PasswordResetTokenRepository;
import com.sky_ecommerce.auth.domain.User;
import com.sky_ecommerce.auth.domain.UserRepository;
import com.sky_ecommerce.auth.service.mail.MailService;
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
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;
    private final Random random = new Random();

    // Explicit constructor to satisfy compilers/environments where Lombok may not be active
    public AuthService(
            UserRepository userRepository,
            OtpTokenRepository otpTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            MailService mailService
    ) {
        this.userRepository = userRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailService = mailService;
    }


    @Transactional
    public SignupResponse signup(SignupRequest req) {
        Optional<User> existing = userRepository.findByEmail(req.getEmail().toLowerCase());
        if (existing.isPresent()) {
            return new SignupResponse(null, "ALREADY_EXISTS", "Email already registered");
        }

        User user = new User()
                .setEmail(req.getEmail().toLowerCase())
                // NOTE: store raw for skeleton; real impl should hash
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

        // Send OTP email (best-effort)
        try {
            if (mailService != null) {
                mailService.sendOtpEmail(user.getEmail(), code);
            }
        } catch (Exception ignored) {
            // log in real impl
        }

        return new SignupResponse(user.getId(), "PENDING", "You have successfully registered, please check your email to verify");
    }

    @Transactional
    public boolean verifyOtp(VerifyOtpRequest req) {
        Instant now = Instant.now();
        return otpTokenRepository
                .findFirstByUserIdAndCodeAndUsedFalseAndExpiresAtAfter(req.getUserId(), req.getCode(), now)
                .map(token -> {
                    token.setUsed(true);
                    otpTokenRepository.save(token);
                    userRepository.findById(req.getUserId()).ifPresent(u -> {
                        u.setStatus(User.UserStatus.ACTIVE);
                        userRepository.save(u);
                        // Send welcome email (best-effort)
                        try {
                            if (mailService != null) {
                                mailService.sendWelcomeEmail(u.getEmail());
                            }
                        } catch (Exception ignored) {
                            // log in real impl
                        }
                    });
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

    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) return;
        String normalized = email.toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalized);
        if (userOpt.isEmpty()) {
            // Always return OK to avoid user enumeration
            return;
        }
        User user = userOpt.get();

        // Invalidate old tokens for this user
        passwordResetTokenRepository.deleteByUserId(user.getId());

        // Generate token
        String resetToken = java.util.UUID.randomUUID().toString().replace("-", "")
                + Long.toHexString(System.nanoTime());

        PasswordResetToken prt = new PasswordResetToken()
                .setUserId(user.getId())
                .setToken(resetToken)
                .setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .setUsed(false);

        passwordResetTokenRepository.save(prt);

        try {
            if (mailService != null) {
                mailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            }
        } catch (Exception ignored) {
            // log in real implementation
        }
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            return false;
        }

        Instant now = Instant.now();
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository
                .findFirstByTokenAndUsedFalseAndExpiresAtAfter(token, now);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken prt = tokenOpt.get();
        Optional<User> userOpt = userRepository.findById(prt.getUserId());
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        // NOTE: skeleton stores raw password; in production, hash with BCryptPasswordEncoder
        user.setPasswordHash(newPassword);
        userRepository.save(user);

        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);

        return true;
    }

    @Transactional
    public LoginResponse login(LoginRequest request, String clientIp) {
        String email = request.getEmail() != null ? request.getEmail().toLowerCase() : null;
        if (email == null || request.getPassword() == null) {
            return new LoginResponse(null, 0L);
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new LoginResponse(null, 0L);
        }

        User user = userOpt.get();
        // NOTE: In a real application, compare password hash. Here we compare raw for the skeleton.
        if (!request.getPassword().equals(user.getPasswordHash())) {
            return new LoginResponse(null, 0L);
        }

        // Enforce ACTIVE accounts for login
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            return new LoginResponse(null, 0L);
        }

        // Send login notification email (best-effort)
        try {
            if (mailService != null) {
                mailService.sendLoginNotification(user.getEmail(), clientIp);
            }
        } catch (Exception ignored) {
            // log in real impl
        }

        // Temporary token until JWT is added: random UUID, 15 minutes expiry
        String token = java.util.UUID.randomUUID().toString();
        long expires = 900L; // seconds

        return new LoginResponse(token, expires);
    }
}
