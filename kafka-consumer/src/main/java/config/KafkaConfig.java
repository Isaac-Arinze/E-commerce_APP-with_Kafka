package config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "order-consumer-group");
        configProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "order-consumer-client");

        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);

        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);

        configProps.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 300000);
        configProps.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 1000);
        configProps.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);

        configProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        configProps.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        props.setSyncCommits(true);

        // Error Handler
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            (record, ex) -> {
                logger.error("❌ Error in consuming message: '{}', offset: {}, exception: {}",
                        record.value(), record.offset(), ex.getMessage(), ex);
            },
            new FixedBackOff(1000L, 3) // Retry 3 times with 1 sec delay
        );

        factory.setCommonErrorHandler(errorHandler);
        factory.setConcurrency(1);
        props.setMissingTopicsFatal(false);

        return factory;
    }
}
