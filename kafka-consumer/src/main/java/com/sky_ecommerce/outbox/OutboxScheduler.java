package com.sky_ecommerce.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduler {
    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxService outboxService;

    public OutboxScheduler(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    // Relay unsent outbox rows every second
    @Scheduled(fixedDelayString = "${outbox.relay.interval-ms:1000}")
    public void relay() {
        try {
            outboxService.relayBatch();
        } catch (Exception e) {
            log.error("Outbox relay failed", e);
        }
    }
}
