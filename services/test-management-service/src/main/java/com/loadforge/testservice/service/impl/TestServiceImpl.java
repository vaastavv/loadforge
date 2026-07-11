package com.loadforge.testservice.service.impl;

import com.loadforge.testservice.domain.Execution;
import com.loadforge.testservice.domain.ExecutionStatus;
import com.loadforge.testservice.domain.Test;
import com.loadforge.testservice.dto.CreateTestRequest;
import com.loadforge.testservice.dto.ExecutionResponse;
import com.loadforge.testservice.dto.TestExecutionJob;
import com.loadforge.testservice.dto.TestResponse;
import com.loadforge.testservice.dto.TestStatusResponse;
import com.loadforge.testservice.exception.ResourceNotFoundException;
import com.loadforge.testservice.exception.TestStateConflictException;
import com.loadforge.testservice.mapper.ExecutionMapper;
import com.loadforge.testservice.mapper.TestMapper;
import com.loadforge.testservice.messaging.TestExecutionProducer;
import com.loadforge.testservice.repository.ExecutionRepository;
import com.loadforge.testservice.repository.TestRepository;
import com.loadforge.testservice.service.TestService;
import com.loadforge.testservice.service.WorkerAssignmentService;
import com.loadforge.testservice.sla.SlaValidationResponse;
import com.loadforge.testservice.sla.SlaValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final TestRepository testRepository;
    private final ExecutionRepository executionRepository;
    private final TestExecutionProducer testExecutionProducer;
    private final WorkerAssignmentService workerAssignmentService;
    private final SlaValidationService slaValidationService;

    @Override
    @Transactional
    public TestResponse createTest(CreateTestRequest request) {
        Test saved = testRepository.save(TestMapper.toEntity(request));
        log.info("Created test {} ({})", saved.getId(), saved.getName());
        return TestMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExecutionResponse startTest(UUID testId) {
        Test test = getTestOrThrow(testId);

        if (executionRepository.existsByTest_IdAndStatus(testId, ExecutionStatus.RUNNING)) {
            throw new TestStateConflictException(
                    "Test %s already has a running execution".formatted(testId));
        }

        UUID assignedWorkerId = workerAssignmentService.selectWorker().orElse(null);

        Execution execution = Execution.builder()
                .test(test)
                .status(ExecutionStatus.RUNNING)
                .assignedWorkerId(assignedWorkerId)
                .startedAt(Instant.now())
                .build();

        Execution saved = executionRepository.save(execution);
        if (assignedWorkerId != null) {
            log.info("Started execution {} for test {} on worker {}", saved.getId(), testId, assignedWorkerId);
        } else {
            log.warn("Started execution {} for test {} with no available worker; awaiting assignment",
                    saved.getId(), testId);
        }

        TestExecutionJob job = new TestExecutionJob(
                saved.getId(),
                test.getId(),
                test.getTargetUrl(),
                test.getHttpMethod(),
                test.getVirtualUsers(),
                test.getDurationSeconds(),
                assignedWorkerId,
                Instant.now());
        testExecutionProducer.publishJob(job);

        return ExecutionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExecutionResponse stopTest(UUID testId) {
        getTestOrThrow(testId);

        Execution execution = executionRepository
                .findFirstByTest_IdAndStatusOrderByStartedAtDesc(testId, ExecutionStatus.RUNNING)
                .orElseThrow(() -> new TestStateConflictException(
                        "Test %s has no running execution to stop".formatted(testId)));

        execution.setStatus(ExecutionStatus.STOPPED);
        execution.setFinishedAt(Instant.now());
        Execution saved = executionRepository.save(execution);
        log.info("Stopped execution {} for test {}", saved.getId(), testId);

        SlaValidationResponse sla = slaValidationService.validateExecution(saved.getId());
        log.info("SLA validation for execution {}: {}", saved.getId(), sla.status());

        return ExecutionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TestStatusResponse getTestStatus(UUID testId) {
        Test test = getTestOrThrow(testId);

        return executionRepository.findFirstByTest_IdOrderByStartedAtDesc(testId)
                .map(exec -> new TestStatusResponse(
                        test.getId(),
                        test.getName(),
                        exec.getStatus().name(),
                        exec.getStatus() == ExecutionStatus.RUNNING ? exec.getId() : null,
                        exec.getStartedAt()))
                .orElseGet(() -> new TestStatusResponse(
                        test.getId(), test.getName(), "NO_EXECUTIONS", null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestResponse> listTests() {
        return testRepository.findAll().stream()
                .map(TestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionResponse> listAllExecutions() {
        return executionRepository.findAllByOrderByStartedAtDesc().stream()
                .map(ExecutionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionResponse> listExecutions(UUID testId) {
        getTestOrThrow(testId);
        return executionRepository.findByTest_IdOrderByStartedAtDesc(testId)
                .stream()
                .map(ExecutionMapper::toResponse)
                .toList();
    }

    private Test getTestOrThrow(UUID testId) {
        return testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test not found: %s".formatted(testId)));
    }
}
