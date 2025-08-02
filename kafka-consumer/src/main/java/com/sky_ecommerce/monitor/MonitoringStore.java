package com.sky_ecommerce.monitor;

import com.sky_ecommerce.common.EventEnvelope;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory ring buffers to store recent messages per topic for monitoring.
 */
@Component
public class MonitoringStore {

    public static final int DEFAULT_CAPACITY = 500;

    private final Map<String, BoundedDeque<MessageView>> topicBuffers = new ConcurrentHashMap<>();
    private final Map<String, BoundedDeque<MessageView>> dltBuffers = new ConcurrentHashMap<>();

    public void record(ConsumerRecord<String, EventEnvelope> record) {
        recordInternal(topicBuffers, record);
    }

    public void recordDlt(ConsumerRecord<String, EventEnvelope> record) {
        recordInternal(dltBuffers, record);
    }

    private void recordInternal(Map<String, BoundedDeque<MessageView>> store, ConsumerRecord<String, EventEnvelope> record) {
        String topic = record.topic();
        store.computeIfAbsent(topic, t -> new BoundedDeque<>(DEFAULT_CAPACITY))
                .add(new MessageView(
                        topic,
                        record.partition(),
                        record.offset(),
                        record.key(),
                        null,
                        record.timestamp(),
                        record.value()
                ));
    }

    public List<MessageView> recent(String topic, int limit) {
        return recentFrom(topicBuffers, topic, limit);
    }

    public List<MessageView> recentDlt(String topic, int limit) {
        return recentFrom(dltBuffers, topic, limit);
    }

    public List<String> topics() {
        var list = new ArrayList<String>();
        list.addAll(topicBuffers.keySet());
        Collections.sort(list);
        return list;
    }

    public List<String> dltTopics() {
        var list = new ArrayList<String>();
        list.addAll(dltBuffers.keySet());
        Collections.sort(list);
        return list;
    }

    private static List<MessageView> recentFrom(Map<String, BoundedDeque<MessageView>> store, String topic, int limit) {
        var dq = store.get(topic);
        if (dq == null) return List.of();
        return dq.tail(limit);
    }

    public record MessageView(
            String topic,
            int partition,
            long offset,
            String key,
            String type,
            long timestamp,
            EventEnvelope payload
    ) {
        public Instant instant() {
            return Instant.ofEpochMilli(timestamp);
        }
    }

    static class BoundedDeque<T> {
        private final int capacity;
        private final Deque<T> deque = new ArrayDeque<>();

        BoundedDeque(int capacity) {
            this.capacity = capacity;
        }

        synchronized void add(T item) {
            if (deque.size() == capacity) {
                deque.removeFirst();
            }
            deque.addLast(item);
        }

        synchronized List<T> tail(int limit) {
            int size = deque.size();
            int n = Math.min(limit, size);
            var result = new ArrayList<T>(n);
            int skip = size - n;
            int i = 0;
            for (T t : deque) {
                if (i++ < skip) continue;
                result.add(t);
            }
            return result;
        }
    }
}
