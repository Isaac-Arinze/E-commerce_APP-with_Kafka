package com.sky_ecommerce.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "otp_tokens", indexes = {
        @Index(name = "ix_otp_user", columnList = "user_id"),
        @Index(name = "ix_otp_user_code_active", columnList = "user_id, code, used, expires_at")
})
public class OtpToken {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public OtpToken() {
        // JPA
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public String getId() {
        return id;
    }

    public OtpToken setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public OtpToken setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getCode() {
        return code;
    }

    public OtpToken setCode(String code) {
        this.code = code;
        return this;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public OtpToken setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public boolean isUsed() {
        return used;
    }

    public OtpToken setUsed(boolean used) {
        this.used = used;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OtpToken setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OtpToken that = (OtpToken) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return (id == null) ? 0 : id.hashCode();
    }
}
