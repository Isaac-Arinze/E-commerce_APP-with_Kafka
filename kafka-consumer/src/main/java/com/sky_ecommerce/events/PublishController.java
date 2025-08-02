package com.sky_ecommerce.events;

import com.sky_ecommerce.common.EventEnvelope;
import com.sky_ecommerce.common.EventEnvelopeFactory;
import com.sky_ecommerce.order.api.CreateOrderRequest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class PublishController {

    private final KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate;
    private final EventEnvelopeFactory envelopeFactory;

    public PublishController(KafkaTemplate<String, EventEnvelope<?>> kafkaTemplate,
                             EventEnvelopeFactory envelopeFactory) {
        this.kafkaTemplate = kafkaTemplate;
        this.envelopeFactory = envelopeFactory;
    }

    // POST /api/events/order
    @PostMapping(path = "/order", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> publishOrder(@RequestBody CreateOrderRequest request,
                                            @RequestParam(value = "key", required = false) String key) {
        String aggregateId = (key != null && !key.isEmpty()) ? key : UUID.randomUUID().toString();
        EventEnvelope<?> envelope = envelopeFactory.create(
                "ORDER_CREATED",   // eventType
                "v1",              // eventVersion
                "ORDER",           // aggregateType
                aggregateId,       // aggregateId
                0,                 // sequence
                request            // payload
        );
        kafkaTemplate.send("order.events", key, envelope);
        return Map.of(
                "status", "SENT",
                "topic", "order.events",
                "key", key,
                "eventType", "ORDER_CREATED",
                "timestamp", Instant.now().toString()
        );
    }

    // Generic publisher for other domains: payment, inventory, notification
    // Body shape: { "key": "optionalKey", "type": "EVENT_TYPE", "payload": { ... } }
    public record GenericEventRequest(String key, String type, Object payload) {}

    @PostMapping(path = "/{domain}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> publishGeneric(@PathVariable("domain") String domain,
                                              @RequestBody GenericEventRequest body) {
        String topic = switch (domain) {
            case "payment" -> "payment.events";
            case "inventory" -> "inventory.events";
            case "notification" -> "notification.events";
            case "order" -> "order.events"; // covered above but allow here too
            default -> throw new IllegalArgumentException("Unsupported domain: " + domain);
        };

        String eventType = StringUtils.hasText(body.type()) ? body.type() : (domain.toUpperCase() + "_EVENT");
        String aggregateType = domain.toUpperCase();
        String aggregateId = (StringUtils.hasText(body.key()) ? body.key() : UUID.randomUUID().toString());
        EventEnvelope<?> envelope = envelopeFactory.create(
                eventType,       // eventType
                "v1",            // eventVersion
                aggregateType,   // aggregateType
                aggregateId,     // aggregateId
                0,               // sequence
                body.payload()   // payload
        );
        kafkaTemplate.send(topic, body.key(), envelope);
        return Map.of(
                "status", "SENT",
                "topic", topic,
                "key", body.key(),
                "eventType", eventType,
                "timestamp", Instant.now().toString()
        );
    }
}
