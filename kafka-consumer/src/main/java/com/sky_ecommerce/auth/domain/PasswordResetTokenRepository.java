package com.sky_ecommerce.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findFirstByTokenAndUsedFalseAndExpiresAtAfter(String token, Instant now);

    void deleteByUserId(String userId);
}
