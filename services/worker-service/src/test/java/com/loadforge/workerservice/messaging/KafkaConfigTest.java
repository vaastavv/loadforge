package com.loadforge.workerservice.messaging;

import com.loadforge.workerservice.dto.TestExecutionJob;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConfigTest {

    @Test
    void testExecutionJobDeserializerShouldParsePayload() throws Exception {
        KafkaConfig kafkaConfig = new KafkaConfig();
        ConsumerFactory<String, TestExecutionJob> consumerFactory = kafkaConfig.testExecutionConsumerFactory("localhost:9092");
        JsonDeserializer<TestExecutionJob> deserializer = (JsonDeserializer<TestExecutionJob>) consumerFactory.getValueDeserializer();

        UUID executionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID testId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID workerId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        String payload = """
                {
                  "executionId": "%s",
                  "testId": "%s",
                  "targetUrl": "http://example.com",
                  "httpMethod": "GET",
                  "virtualUsers": 2,
                  "durationSeconds": 5,
                  "assignedWorkerId": "%s",
                  "dispatchedAt": "2026-07-11T00:00:00Z"
                }
                """.formatted(executionId, testId, workerId);

        TestExecutionJob job = deserializer.deserialize("test-execution", payload.getBytes(StandardCharsets.UTF_8));

        assertThat(job).isNotNull();
        assertThat(job.executionId()).isEqualTo(executionId);
        assertThat(job.testId()).isEqualTo(testId);
        assertThat(job.assignedWorkerId()).isEqualTo(workerId);
    }
}
