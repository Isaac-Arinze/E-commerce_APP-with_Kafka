package com.sky_ecommerce.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, String> {

    Optional<OtpToken> findFirstByUserIdAndCodeAndUsedFalseAndExpiresAtAfter(String userId, String code, Instant now);

    List<OtpToken> findByUserIdAndUsedFalse(String userId);
}
