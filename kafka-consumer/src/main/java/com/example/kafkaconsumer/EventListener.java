package com.example.kafkaconsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class EventListener {

    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

    @KafkaListener(
        topics = "test-topic",
        groupId = "event-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        logger.info("📨 Received event from topic: {}, partition: {}, offset: {}", topic, partition, offset);

        try {
            processEvent(message);

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                logger.info("✅ Acknowledged event message at offset {}", offset);
            }

        } catch (Exception ex) {
            logger.error("❌ Failed to process event: '{}', Error: {}", message, ex.getMessage(), ex);
        }
    }

    private void processEvent(String message) throws Exception {
        logger.info("🎯 Processing event: {}", message);

        // Simulated processing
        if (message.contains("fail")) {
            throw new Exception("Simulated event failure");
        }

        Thread.sleep(100); // Simulated delay
    }
}
