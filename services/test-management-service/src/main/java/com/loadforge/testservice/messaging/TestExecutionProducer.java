package com.loadforge.testservice.messaging;

import com.loadforge.testservice.dto.TestExecutionJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes test execution jobs to the {@code test-execution} topic for workers to consume.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestExecutionProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishJob(TestExecutionJob job) {
        log.info("Publishing test execution job {} for test {} to topic {}",
                job.executionId(), job.testId(), KafkaConfig.TEST_EXECUTION_TOPIC);
        kafkaTemplate.send(KafkaConfig.TEST_EXECUTION_TOPIC, job.testId().toString(), job);
    }
}
