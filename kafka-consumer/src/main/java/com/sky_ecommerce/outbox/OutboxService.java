package com.sky_ecommerce.outbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky_ecommerce.common.EventEnvelope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxService {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate;

    public OutboxService(OutboxRepository repository, ObjectMapper objectMapper, KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void append(String topic, String key, EventEnvelope<?> envelope) {
        try {
            OutboxEntity e = new OutboxEntity();
            e.setId(envelope.getId());
            e.setTopic(topic);
            e.setKey(key);
            e.setEnvelopeJson(objectMapper.writeValueAsString(envelope));
            e.setCreatedAt(Instant.now());
            e.setAttempts(0);
            e.setStatus("PENDING");
            repository.save(e);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to append outbox record", ex);
        }
    }

    @Transactional
    public void markSent(OutboxEntity e) {
        e.setSentAt(Instant.now());
        e.setStatus("SENT");
        repository.save(e);
    }

    @Transactional
    public void markFailed(OutboxEntity e, Exception ex) {
        e.setAttempts(e.getAttempts() + 1);
        if (e.getAttempts() > 10) {
            e.setStatus("FAILED");
        }
        repository.save(e);
    }

    @Transactional
    public void relayBatch() {
        Pageable page = PageRequest.of(0, 50);
        List<OutboxEntity> batch = repository.findPending(page);
        for (OutboxEntity e : batch) {
            try {
                EventEnvelope<?> env = objectMapper.readValue(e.getEnvelopeJson(), new TypeReference<EventEnvelope<?>>() {});
                kafkaTemplate
                        .send(e.getTopic(), e.getKey(), env)
                        .whenComplete((result, throwable) -> {
                            if (throwable == null) {
                                markSent(e);
                            } else {
                                Exception exToRecord = (throwable instanceof Exception)
                                        ? (Exception) throwable
                                        : new RuntimeException(throwable);
                                markFailed(e, exToRecord);
                            }
                        });
            } catch (Exception ex) {
                markFailed(e, ex);
            }
        }
    }
}
