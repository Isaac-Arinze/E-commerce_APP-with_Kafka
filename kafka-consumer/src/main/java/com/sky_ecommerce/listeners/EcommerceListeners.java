package com.sky_ecommerce.listeners;

import com.sky_ecommerce.common.EventEnvelope;
import com.sky_ecommerce.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

/**
 * Simulated internal services (Payment, Inventory, Notification) wired as Kafka listeners
 * inside this single application. In a real microservices setup, each would be a separate service.
 */
@Service
public class EcommerceListeners {

    private static final Logger log = LoggerFactory.getLogger(EcommerceListeners.class);
    private final OrderService orderService;
    private final Random random = new Random();

    public EcommerceListeners(OrderService orderService) {
        this.orderService = orderService;
    }

    // Payment Service simulation:
    // Consumes OrderCreated events from order.events
    // Emits PaymentSucceeded/PaymentFailed indirectly by calling OrderService methods which write outbox (OrderPaid/OrderCancelled)
    @KafkaListener(
            topics = "${topics.order-events:order.events}",
            groupId = "payment-simulator",
            containerFactory = "ecommerceKafkaListenerContainerFactory"
    )
    public void onOrderCreated(@Payload EventEnvelope<Map<String, Object>> env, Acknowledgment ack) {
        try {
            if (!"OrderCreated".equals(env.getEventType())) {
                ack.acknowledge();
                return;
            }
            String orderId = String.valueOf(env.getPayload().get("orderId"));
            // Simulate payment decision (80% success)
            boolean success = random.nextInt(10) < 8;
            if (success) {
                log.info("Payment succeeded for order {}", orderId);
                orderService.markPaid(orderId, env.getCorrelationId());
            } else {
                log.warn("Payment failed for order {}", orderId);
                orderService.cancel(orderId, env.getCorrelationId(), "payment-failed");
            }
            ack.acknowledge();
        } catch (Exception e) {
            // Let container handle retries via DefaultErrorHandler by rethrowing
            throw e;
        }
    }

    // Inventory Service simulation:
    // Also consumes OrderCreated; emits StockReserved/StockInsufficient in a real setup.
    // Here, we only log to keep the demo simple.
    @KafkaListener(
            topics = "${topics.order-events:order.events}",
            groupId = "inventory-simulator",
            containerFactory = "ecommerceKafkaListenerContainerFactory"
    )
    public void onOrderCreatedInventory(@Payload EventEnvelope<Map<String, Object>> env, Acknowledgment ack) {
        try {
            if (!"OrderCreated".equals(env.getEventType())) {
                ack.acknowledge();
                return;
            }
            String orderId = String.valueOf(env.getPayload().get("orderId"));
            log.info("Inventory reserved for order {} (simulated)", orderId);
            ack.acknowledge();
        } catch (Exception e) {
            throw e;
        }
    }

    // Notification Service simulation:
    // Consumes OrderPaid and would emit NotificationRequested; here we just log.
    @KafkaListener(
            topics = "${topics.order-events:order.events}",
            groupId = "notification-simulator",
            containerFactory = "ecommerceKafkaListenerContainerFactory"
    )
    public void onOrderPaidNotify(@Payload EventEnvelope<Map<String, Object>> env, Acknowledgment ack) {
        try {
            if (!"OrderPaid".equals(env.getEventType())) {
                ack.acknowledge();
                return;
            }
            String orderId = String.valueOf(env.getPayload().get("orderId"));
            log.info("Notification sent for paid order {} (simulated)", orderId);
            ack.acknowledge();
        } catch (Exception e) {
            throw e;
        }
    }
}
