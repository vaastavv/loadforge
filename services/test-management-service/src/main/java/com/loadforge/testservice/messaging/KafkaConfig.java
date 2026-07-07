package com.loadforge.testservice.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;

/**
 * Kafka configuration: declares the topics owned by the platform and the message converter
 * used to deserialize inbound JSON payloads to each listener's parameter type.
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    public static final String TEST_EXECUTION_TOPIC = "test-execution";
    public static final String WORKER_HEARTBEAT_TOPIC = "worker-heartbeat";
    public static final String METRICS_TOPIC = "metrics";

    @Bean
    public NewTopic testExecutionTopic() {
        return TopicBuilder.name(TEST_EXECUTION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic workerHeartbeatTopic() {
        return TopicBuilder.name(WORKER_HEARTBEAT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic metricsTopic() {
        return TopicBuilder.name(METRICS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Deserializes inbound JSON using the target listener method's parameter type
     * (INFERRED precedence) so a single consumer factory can handle multiple payload types.
     */
    @Bean
    public RecordMessageConverter jsonMessageConverter() {
        StringJsonMessageConverter converter = new StringJsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.addTrustedPackages("com.loadforge.testservice.dto");
        converter.setTypeMapper(typeMapper);
        return converter;
    }
}
