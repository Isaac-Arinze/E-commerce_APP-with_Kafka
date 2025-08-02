package com.monitoring;

import com.example.kafkaconsumer.events.ConsumerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.event.*;
import org.springframework.stereotype.Component;

@Component
public class KafkaHealthMonitor {

    private static final Logger logger = LoggerFactory.getLogger(KafkaHealthMonitor.class);

    @EventListener
    public void handleConsumerStarted(ConsumerStartedEvent event) {
        logger.info("🟢 Kafka consumer started: {}", event.getSource());
    }

    @EventListener
    public void handleConsumerStopped(ConsumerStoppedEvent event) {
        logger.warn("🔴 Kafka consumer stopped: {}", event.getSource());
    }

    @EventListener
    public void handleConsumerPaused(ConsumerPausedEvent event) {
        logger.warn("⏸️ Consumer paused for partitions: {}", event.getPartitions());
    }

    @EventListener
    public void handleConsumerResumed(ConsumerResumedEvent event) {
        logger.info("▶️ Consumer resumed for partitions: {}", event.getPartitions());
    }

    @EventListener
    public void handleListenerContainerIdle(ListenerContainerIdleEvent event) {
        logger.debug("💤 Consumer idle for {} ms on partitions: {}", 
                    event.getIdleTime(), event.getTopicPartitions());
    }

    @EventListener
    public void handleNonResponsiveConsumer(NonResponsiveConsumerEvent event) {
        logger.error("⚠️ Non-responsive consumer detected after {} ms", 
                    event.getTimeSinceLastPoll());
        // This is a critical event - consider alerting
    }

    // Generic event handler for partition events (works with older Spring Kafka versions)
    @EventListener
    public void handleConsumerEvent(ConsumerEvent event) {
        if (event.toString().contains("partition")) {
            logger.info("📋 Consumer partition event: {}", event.getClass().getSimpleName());
        }
    }
}
