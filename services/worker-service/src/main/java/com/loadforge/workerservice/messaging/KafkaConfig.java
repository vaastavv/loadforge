package com.loadforge.workerservice.messaging;

import com.loadforge.workerservice.dto.TestExecutionJob;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, TestExecutionJob> testExecutionConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "worker-service");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Configure the JSON value deserializer via the instance below only. Spring Kafka
        // rejects a JsonDeserializer that is configured both by property setters AND by
        // configuration properties, so the JsonDeserializer.* entries must NOT be added to
        // the props map (that failed the consumer at startup with an IllegalStateException).
        JsonDeserializer<TestExecutionJob> deserializer = new JsonDeserializer<>(TestExecutionJob.class);
        deserializer.addTrustedPackages("com.loadforge.workerservice.dto");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TestExecutionJob> testExecutionListenerFactory(
            ConsumerFactory<String, TestExecutionJob> testExecutionConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TestExecutionJob> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(testExecutionConsumerFactory);
        factory.setConcurrency(1);
        return factory;
    }
}
