package com.loadforge.workerservice.messaging;

import com.loadforge.workerservice.dto.TestExecutionJob;
import com.loadforge.workerservice.service.LoadGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestExecutionConsumer {

    private final LoadGeneratorService loadGeneratorService;

    @KafkaListener(
            topics = "test-execution",
            groupId = "worker-service",
            containerFactory = "testExecutionListenerFactory"
    )
    public void onTestExecution(TestExecutionJob job) {
        log.info("Received execution job {} for test {} assigned to worker {}",
                job.executionId(), job.testId(), job.assignedWorkerId());
        loadGeneratorService.submit(job);
    }
}
