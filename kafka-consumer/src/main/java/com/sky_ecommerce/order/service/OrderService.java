package com.sky_ecommerce.order.service;

import com.sky_ecommerce.common.EventEnvelope;
import com.sky_ecommerce.common.EventEnvelopeFactory;
import com.sky_ecommerce.order.api.CreateOrderRequest;
import com.sky_ecommerce.order.domain.Order;
import com.sky_ecommerce.order.domain.OrderRepository;
import com.sky_ecommerce.outbox.OutboxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    private final EventEnvelopeFactory envelopeFactory;

    @Value("${topics.order-events:order.events}")
    private String orderTopic;

    public OrderService(OrderRepository orderRepository,
                        OutboxService outboxService,
                        EventEnvelopeFactory envelopeFactory) {
        this.orderRepository = orderRepository;
        this.outboxService = outboxService;
        this.envelopeFactory = envelopeFactory;
    }

    @Transactional
    public String createOrder(CreateOrderRequest req, String correlationId) {
        Order order = new Order();
        String orderId = UUID.randomUUID().toString();
        order.setId(orderId);
        order.setCustomerId(req.getCustomerId());
        order.setItems(req.getItems().stream().map(li -> {
            Order.LineItem x = new Order.LineItem();
            x.setSku(li.getSku());
            x.setQuantity(li.getQuantity());
            x.setPrice(li.getPrice());
            return x;
        }).toList());
        order.setTotal(req.getItems().stream()
                .map(li -> li.getPrice().multiply(BigDecimal.valueOf(li.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        order.setStatus(Order.Status.PENDING);

        orderRepository.save(order);

        // Build domain event payload decoupled from controller/business model
        com.sky_ecommerce.order.service.events.OrderEvents.OrderCreated payload =
                new com.sky_ecommerce.order.service.events.OrderEvents.OrderCreated(order.getId(), order.getCustomerId(), order.getTotal());

        EventEnvelope<com.sky_ecommerce.order.service.events.OrderEvents.OrderCreated> env = envelopeFactory.create(
                "OrderCreated",         // eventType
                "v1",                   // eventVersion
                "ORDER",                // aggregateType
                order.getId(),          // aggregateId
                1,                      // sequence
                payload                 // payload
        );
        outboxService.append(orderTopic, order.getId(), env);
        return orderId;
    }

    @Transactional
    public void markPaid(String orderId, String correlationId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(Order.Status.PAID);
        orderRepository.save(order);

        com.sky_ecommerce.order.service.events.OrderEvents.OrderPaid payload =
                new com.sky_ecommerce.order.service.events.OrderEvents.OrderPaid(orderId);

        EventEnvelope<com.sky_ecommerce.order.service.events.OrderEvents.OrderPaid> env = envelopeFactory.create(
                "OrderPaid",            // eventType
                "v1",                   // eventVersion
                "ORDER",                // aggregateType
                orderId,                // aggregateId
                1,                      // sequence
                payload                 // payload
        );
        outboxService.append(orderTopic, orderId, env);
    }

    @Transactional
    public void cancel(String orderId, String correlationId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(Order.Status.CANCELLED);
        orderRepository.save(order);

        com.sky_ecommerce.order.service.events.OrderEvents.OrderCancelled payload =
                new com.sky_ecommerce.order.service.events.OrderEvents.OrderCancelled(orderId, reason);

        EventEnvelope<com.sky_ecommerce.order.service.events.OrderEvents.OrderCancelled> env = envelopeFactory.create(
                "OrderCancelled",       // eventType
                "v1",                   // eventVersion
                "ORDER",                // aggregateType
                orderId,                // aggregateId
                1,                      // sequence
                payload                 // payload
        );
        outboxService.append(orderTopic, orderId, env);
    }

    // Event payloads moved to dedicated package to enforce separation of concerns.
}
