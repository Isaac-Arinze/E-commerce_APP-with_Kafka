package com.sky_ecommerce.outbox;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "outbox")
public class OutboxEntity {

    @Id
    @Column(length = 64)
    private String id; // same as envelope id

    @Column(nullable = false, length = 200)
    private String topic;

    // "key" is a reserved keyword in H2; map the column to a different name
    @Column(name = "message_key", nullable = false, length = 200)
    private String key; // partitioning key, e.g., orderId

    @Lob
    @Column(nullable = false)
    private String envelopeJson;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant sentAt;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false, length = 32)
    private String status; // PENDING, SENT, FAILED

    public String getId() { return id; }
    public String getTopic() { return topic; }
    public String getKey() { return key; }
    public String getEnvelopeJson() { return envelopeJson; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSentAt() { return sentAt; }
    public Integer getAttempts() { return attempts; }
    public String getStatus() { return status; }

    public void setId(String id) { this.id = id; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setKey(String key) { this.key = key; }
    public void setEnvelopeJson(String envelopeJson) { this.envelopeJson = envelopeJson; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public void setStatus(String status) { this.status = status; }
}
