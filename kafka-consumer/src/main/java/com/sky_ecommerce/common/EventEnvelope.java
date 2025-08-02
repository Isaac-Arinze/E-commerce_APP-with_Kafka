package com.sky_ecommerce.common;

import java.time.Instant;
import java.util.Objects;

public class EventEnvelope<T> {
    private String id;
    private Integer schemaVersion;
    private String eventType;
    private String subjectId; // e.g., orderId
    private Instant occurredAt;
    private String correlationId;
    private String source; // service name/module name
    private T payload;

    public EventEnvelope() {}

    public EventEnvelope(String id, Integer schemaVersion, String eventType, String subjectId, Instant occurredAt, String correlationId, String source, T payload) {
        this.id = id;
        this.schemaVersion = schemaVersion;
        this.eventType = eventType;
        this.subjectId = subjectId;
        this.occurredAt = occurredAt;
        this.correlationId = correlationId;
        this.source = source;
        this.payload = payload;
    }

    public String getId() { return id; }
    public Integer getSchemaVersion() { return schemaVersion; }
    public String getEventType() { return eventType; }
    public String getSubjectId() { return subjectId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getCorrelationId() { return correlationId; }
    public String getSource() { return source; }
    public T getPayload() { return payload; }

    public void setId(String id) { this.id = id; }
    public void setSchemaVersion(Integer schemaVersion) { this.schemaVersion = schemaVersion; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public void setSource(String source) { this.source = source; }
    public void setPayload(T payload) { this.payload = payload; }

    @Override
    public String toString() {
        return "EventEnvelope{" +
                "id='" + id + '\'' +
                ", schemaVersion=" + schemaVersion +
                ", eventType='" + eventType + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", occurredAt=" + occurredAt +
                ", correlationId='" + correlationId + '\'' +
                ", source='" + source + '\'' +
                ", payload=" + Objects.toString(payload) +
                '}';
    }
}
