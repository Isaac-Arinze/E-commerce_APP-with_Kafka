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
public class OrderListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderListener.class);

    @KafkaListener(
        topics = "order.placed",
        groupId = "order-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        logger.info("📥 Received message from topic: {}, partition: {}, offset: {}", topic, partition, offset);

        try {
            // Business logic
            processOrder(message);

            // Acknowledge after successful processing
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                logger.info("✅ Acknowledged message at offset {}", offset);
            }

        } catch (Exception ex) {
            logger.error("❌ Failed to process message: '{}', Error: {}", message, ex.getMessage(), ex);
            handleProcessingError(message, topic, partition, offset, ex);
        }
    }

    private void processOrder(String message) throws Exception {
        // TODO: Replace with real processing logic
        logger.info("🚚 Processing order: {}", message);

        // Simulate success/failure
        if (message.contains("fail")) {
            throw new Exception("Simulated processing failure");
        }

        // Simulate delay
        Thread.sleep(100);
    }

    private void handleProcessingError(String message, String topic, int partition, long offset, Exception e) {
        // TODO: Implement custom error handling, like sending to DLQ
        logger.warn("⚠️ Handling error for message: {}, topic: {}, partition: {}, offset: {}",
                    message, topic, partition, offset);
    }
}
