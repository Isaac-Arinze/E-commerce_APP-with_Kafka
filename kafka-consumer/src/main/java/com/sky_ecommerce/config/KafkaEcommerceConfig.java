package com.sky_ecommerce.config;

import com.sky_ecommerce.common.EventEnvelope;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
// import org.springframework.util.backoff.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaEcommerceConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    // Producer

    @Bean
    public ProducerFactory<String, EventEnvelope<?>> ecommerceProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // batching hints
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, EventEnvelope<?>> ecommerceKafkaTemplate() {
        KafkaTemplate<String, EventEnvelope<?>> template = new KafkaTemplate<>(ecommerceProducerFactory());
        template.setObservationEnabled(true);
        return template;
    }

    // Consumer

    @Bean
    public ConsumerFactory<String, EventEnvelope> ecommerceConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.sky_ecommerce");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 600000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(EventEnvelope.class, false)
        );
    }

    @Bean(name = "ecommerceKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> ecommerceKafkaListenerContainerFactory(
            ConsumerFactory<String, EventEnvelope> cf,
            KafkaTemplate<String, EventEnvelope<?>> template
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, EventEnvelope>();
        factory.setConsumerFactory(cf);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(template, (record, ex) ->
                        new TopicPartition(record.topic() + ".DLT", record.partition()));

        ExponentialBackOff backoff = new ExponentialBackOff();
        backoff.setInitialInterval(1000);
        backoff.setMultiplier(2.0);
        backoff.setMaxInterval(10000);

        CommonErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backoff);
        factory.setCommonErrorHandler(errorHandler);
        factory.setConcurrency(3);
        factory.getContainerProperties().setMissingTopicsFatal(false);
        return factory;
    }

    // Optional topic beans (only effective with Kafka Admin enabled; else create via CLI)
    @Bean
    public NewTopic orderEventsTopic(@Value("${topics.order-events:order.events}") String name) {
        return new NewTopic(name, 3, (short) 1);
    }

    @Bean
    public NewTopic paymentEventsTopic(@Value("${topics.payment-events:payment.events}") String name) {
        return new NewTopic(name, 3, (short) 1);
    }

    @Bean
    public NewTopic inventoryEventsTopic(@Value("${topics.inventory-events:inventory.events}") String name) {
        return new NewTopic(name, 3, (short) 1);
    }

    @Bean
    public NewTopic notificationEventsTopic(@Value("${topics.notification-events:notification.events}") String name) {
        return new NewTopic(name, 3, (short) 1);
    }

    @Bean
    public NewTopic orderEventsDlt(@Value("${topics.order-events:order.events}") String name) {
        return new NewTopic(name + ".DLT", 3, (short) 1);
    }

    @Bean
    public NewTopic paymentEventsDlt(@Value("${topics.payment-events:payment.events}") String name) {
        return new NewTopic(name + ".DLT", 3, (short) 1);
    }

    @Bean
    public NewTopic inventoryEventsDlt(@Value("${topics.inventory-events:inventory.events}") String name) {
        return new NewTopic(name + ".DLT", 3, (short) 1);
    }

    @Bean
    public NewTopic notificationEventsDlt(@Value("${topics.notification-events:notification.events}") String name) {
        return new NewTopic(name + ".DLT", 3, (short) 1);
    }
}
