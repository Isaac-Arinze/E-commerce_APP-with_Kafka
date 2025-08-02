package com.sky_ecommerce.common;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class EventEnvelopeFactory {

    public <T> EventEnvelope<T> create(String eventType,
                                       String subjectId,
                                       String source,
                                       String correlationId,
                                       int schemaVersion,
                                       T payload) {
        EventEnvelope<T> env = new EventEnvelope<>();
        env.setId(UUID.randomUUID().toString());
        env.setSchemaVersion(schemaVersion);
        env.setEventType(eventType);
        env.setSubjectId(subjectId);
        env.setOccurredAt(Instant.now());
        env.setCorrelationId(correlationId != null ? correlationId : UUID.randomUUID().toString());
        env.setSource(source);
        env.setPayload(payload);
        return env;
    }
}
