package com.loadforge.testservice.service;

import com.loadforge.testservice.domain.Execution;
import com.loadforge.testservice.domain.ExecutionStatus;
import com.loadforge.testservice.domain.HttpMethod;
import com.loadforge.testservice.dto.TestExecutionJob;
import com.loadforge.testservice.messaging.TestExecutionProducer;
import com.loadforge.testservice.repository.ExecutionRepository;
import com.loadforge.testservice.scheduler.WorkerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkerFailoverServiceTest {

    @Mock
    private WorkerRegistry workerRegistry;

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private WorkerAssignmentService workerAssignmentService;

    @Mock
    private TestExecutionProducer testExecutionProducer;

    @InjectMocks
    private WorkerFailoverService service;

    private com.loadforge.testservice.domain.Test test;

    @BeforeEach
    void setUp() {
        test = com.loadforge.testservice.domain.Test.builder()
                .id(UUID.randomUUID())
                .name("Checkout load test")
                .description("Peak traffic scenario")
                .targetUrl("https://example.com/checkout")
                .httpMethod(HttpMethod.POST)
                .virtualUsers(500)
                .durationSeconds(120)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void handleWorkerOffline_reassignsRunningExecutionToHealthyWorker() {
        UUID deadWorker = UUID.randomUUID();
        UUID healthyWorker = UUID.randomUUID();
        Execution execution = runningExecutionOn(deadWorker);

        when(executionRepository.findByAssignedWorkerIdAndStatus(deadWorker, ExecutionStatus.RUNNING))
                .thenReturn(List.of(execution));
        when(workerAssignmentService.selectWorker(Set.of(deadWorker))).thenReturn(Optional.of(healthyWorker));
        when(executionRepository.save(any(Execution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handleWorkerOffline(deadWorker);

        verify(workerRegistry).markUnhealthy(deadWorker);
        assertThat(execution.getAssignedWorkerId()).isEqualTo(healthyWorker);
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.RUNNING);

        ArgumentCaptor<TestExecutionJob> jobCaptor = ArgumentCaptor.forClass(TestExecutionJob.class);
        verify(testExecutionProducer).publishJob(jobCaptor.capture());
        TestExecutionJob job = jobCaptor.getValue();
        assertThat(job.executionId()).isEqualTo(execution.getId());
        assertThat(job.assignedWorkerId()).isEqualTo(healthyWorker);
        assertThat(job.targetUrl()).isEqualTo("https://example.com/checkout");
    }

    @Test
    void handleWorkerOffline_whenNoHealthyWorker_failsExecution() {
        UUID deadWorker = UUID.randomUUID();
        Execution execution = runningExecutionOn(deadWorker);

        when(executionRepository.findByAssignedWorkerIdAndStatus(deadWorker, ExecutionStatus.RUNNING))
                .thenReturn(List.of(execution));
        when(workerAssignmentService.selectWorker(Set.of(deadWorker))).thenReturn(Optional.empty());
        when(executionRepository.save(any(Execution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handleWorkerOffline(deadWorker);

        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.FAILED);
        assertThat(execution.getFinishedAt()).isNotNull();
        assertThat(execution.getErrorMessage()).contains(deadWorker.toString());
        verify(testExecutionProducer, never()).publishJob(any());
    }

    @Test
    void handleWorkerOffline_whenNoRunningExecutions_marksUnhealthyOnly() {
        UUID deadWorker = UUID.randomUUID();
        when(executionRepository.findByAssignedWorkerIdAndStatus(deadWorker, ExecutionStatus.RUNNING))
                .thenReturn(List.of());

        service.handleWorkerOffline(deadWorker);

        verify(workerRegistry).markUnhealthy(deadWorker);
        verify(executionRepository, never()).save(any());
        verify(testExecutionProducer, never()).publishJob(any());
        verifyNoInteractions(workerAssignmentService);
    }

    private Execution runningExecutionOn(UUID workerId) {
        return Execution.builder()
                .id(UUID.randomUUID())
                .test(test)
                .status(ExecutionStatus.RUNNING)
                .assignedWorkerId(workerId)
                .startedAt(Instant.now())
                .createdAt(Instant.now())
                .build();
    }
}
