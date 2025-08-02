package com.sky_ecommerce.monitor;

import com.sky_ecommerce.common.EventEnvelope;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MonitoringController {

    private final MonitoringStore store;
    private final KafkaListenerEndpointRegistry registry;

    public MonitoringController(MonitoringStore store, KafkaListenerEndpointRegistry registry) {
        this.store = store;
        this.registry = registry;
    }

    @GetMapping(path = "/api/monitor/topics", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> topics() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("topics", store.topics());
        resp.put("dltTopics", store.dltTopics());
        return resp;
    }

    @GetMapping(path = "/api/monitor/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MonitoringStore.MessageView> messages(
            @RequestParam("topic") String topic,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return store.recent(topic, limit);
    }

    @GetMapping(path = "/api/monitor/dlt", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MonitoringStore.MessageView> dlt(
            @RequestParam("topic") String topic,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return store.recentDlt(topic, limit);
    }

    @GetMapping(path = "/api/monitor/consumers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> consumers() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("listenerContainerIds", registry.getListenerContainerIds());
        return resp;
    }

    @GetMapping(path = "/api/monitor/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> health() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("status", "UP");
        return resp;
    }
}
