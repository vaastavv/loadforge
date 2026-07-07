package com.loadforge.testservice.service;

import com.loadforge.testservice.domain.Execution;
import com.loadforge.testservice.domain.ExecutionStatus;
import com.loadforge.testservice.domain.HttpMethod;
import com.loadforge.testservice.dto.CreateTestRequest;
import com.loadforge.testservice.dto.ExecutionResponse;
import com.loadforge.testservice.dto.TestExecutionJob;
import com.loadforge.testservice.dto.TestResponse;
import com.loadforge.testservice.dto.TestStatusResponse;
import com.loadforge.testservice.exception.ResourceNotFoundException;
import com.loadforge.testservice.exception.TestStateConflictException;
import com.loadforge.testservice.messaging.TestExecutionProducer;
import com.loadforge.testservice.repository.ExecutionRepository;
import com.loadforge.testservice.repository.TestRepository;
import com.loadforge.testservice.service.impl.TestServiceImpl;
import com.loadforge.testservice.sla.SlaStatus;
import com.loadforge.testservice.sla.SlaValidationResponse;
import com.loadforge.testservice.sla.SlaValidationService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private TestRepository testRepository;

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private TestExecutionProducer testExecutionProducer;

    @Mock
    private WorkerAssignmentService workerAssignmentService;

    @Mock
    private SlaValidationService slaValidationService;

    @InjectMocks
    private TestServiceImpl service;

    private UUID testId;
    private com.loadforge.testservice.domain.Test test;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        test = com.loadforge.testservice.domain.Test.builder()
                .id(testId)
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
    void createTest_persistsAndReturnsResponse() {
        CreateTestRequest request = new CreateTestRequest(
                "Checkout load test", "Peak traffic scenario",
                "https://example.com/checkout", HttpMethod.POST, 500, 120);
        when(testRepository.save(any(com.loadforge.testservice.domain.Test.class))).thenReturn(test);

        TestResponse response = service.createTest(request);

        assertThat(response.id()).isEqualTo(testId);
        assertThat(response.name()).isEqualTo("Checkout load test");
        assertThat(response.httpMethod()).isEqualTo(HttpMethod.POST);
        verify(testRepository).save(any(com.loadforge.testservice.domain.Test.class));
    }

    @Test
    void startTest_whenNoRunningExecution_createsRunningExecutionOnSelectedWorker() {
        UUID workerId = UUID.randomUUID();
        when(testRepository.findById(testId)).thenReturn(Optional.of(test));
        when(executionRepository.existsByTest_IdAndStatus(testId, ExecutionStatus.RUNNING))
                .thenReturn(false);
        when(workerAssignmentService.selectWorker()).thenReturn(Optional.of(workerId));
        when(executionRepository.save(any(Execution.class))).thenAnswer(invocation -> {
            Execution e = invocation.getArgument(0);
            e.setId(UUID.randomUUID());
            e.setCreatedAt(Instant.now());
            return e;
        });

        ExecutionResponse response = service.startTest(testId);

        assertThat(response.status()).isEqualTo(ExecutionStatus.RUNNING);
        assertThat(response.testId()).isEqualTo(testId);

        ArgumentCaptor<Execution> captor = ArgumentCaptor.forClass(Execution.class);
        verify(executionRepository).save(captor.capture());
        assertThat(captor.getValue().getStartedAt()).isNotNull();
        assertThat(captor.getValue().getStatus()).isEqualTo(ExecutionStatus.RUNNING);
        assertThat(captor.getValue().getAssignedWorkerId()).isEqualTo(workerId);

        ArgumentCaptor<TestExecutionJob> jobCaptor = ArgumentCaptor.forClass(TestExecutionJob.class);
        verify(testExecutionProducer).publishJob(jobCaptor.capture());
        assertThat(jobCaptor.getValue().assignedWorkerId()).isEqualTo(workerId);
    }

    @Test
    void startTest_whenNoWorkerAvailable_startsExecutionUnassigned() {
        when(testRepository.findById(testId)).thenReturn(Optional.of(test));
        when(executionRepository.existsByTest_IdAndStatus(testId, ExecutionStatus.RUNNING))
                .thenReturn(false);
        when(workerAssignmentService.selectWorker()).thenReturn(Optional.empty());
        when(executionRepository.save(any(Execution.class))).thenAnswer(invocation -> {
            Execution e = invocation.getArgument(0);
            e.setId(UUID.randomUUID());
            e.setCreatedAt(Instant.now());
            return e;
        });

        ExecutionResponse response = service.startTest(testId);

        assertThat(response.status()).isEqualTo(ExecutionStatus.RUNNING);
        ArgumentCaptor<Execution> captor = ArgumentCaptor.forClass(Execution.class);
        verify(executionRepository).save(captor.capture());
        assertThat(captor.getValue().getAssignedWorkerId()).isNull();
    }

    @Test
    void startTest_whenTestMissing_throwsNotFound() {
        when(testRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startTest(testId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(executionRepository, never()).save(any());
    }

    @Test
    void startTest_whenAlreadyRunning_throwsConflict() {
        when(testRepository.findById(testId)).thenReturn(Optional.of(test));
        when(executionRepository.existsByTest_IdAndStatus(testId, ExecutionStatus.RUNNING))
                .thenReturn(true);

        assertThatThrownBy(() -> service.startTest(testId))
                .isInstanceOf(TestStateConflictException.class);
        verify(executionRepository, never()).save(any());
    }

    @Test
    void stopTest_whenRunningExecution_marksStopped() {
        Execution running = Execution.builder()
                .id(UUID.randomUUID())
                .test(test)
                .status(ExecutionStatus.RUNNING)
                .startedAt(Instant.now())
                .createdAt(Instant.now())
                .build();
        when(testRepository.findById(testId)).thenReturn(Optional.of(test));
        when(executionRepository.findFirstByTest_IdAndStatusOrderByStartedAtDesc(
                testId, ExecutionStatus.RUNNING)).thenReturn(Optional.of(running));
        when(executionRepository.save(any(Execution.class))).thenAnswer(inv -> inv.getArgument(0));
        when(slaValidationService.validateExecution(any())).thenReturn(new SlaValidationResponse(
                running.getId(), SlaStatus.PASS, "default", Instant.now(), List.of()));

        ExecutionResponse response = service.stopTest(testId);

        assertThat(response.status()).isEqualTo(ExecutionStatus.STOPPED);
        assertThat(response.finishedAt()).isNotNull();
        verify(slaValidationService).validateExecution(running.getId());
    }

    @Test
    void stopTest_whenNoRunningExecution_throwsConflict() {
        when(testRepository.findById(testId)).thenReturn(Optional.of(test));
        when(executionRepository.findFirstByTest_IdAndStatusOrderByStartedAtDesc(
                testId, ExecutionStatus.RUNNING)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.stopTest(testId))
                .isInstanceOf(TestStateConflictException.class);
    }

    @Test
    void getTestStatus_whenLatestRunning_returnsRunningStatus() {
        Execution running = Execution.builder()
                .id(UUID.randomUUID())
                .test(test)
                .status(ExecutionStatus.RUNNING)
                .startedAt(Instant.now())
                .createdAt(Instant.now())
                .build();
        when(testRepository.findById(testId)).thenReturn(Optional.of(test));
        when(executionRepository.findFirstByTest_IdOrderByStartedAtDesc(testId))
                .thenReturn(Optional.of(running));

        TestStatusResponse status = service.getTestStatus(testId);

        assertThat(status.status()).isEqualTo("RUNNING");
        assertThat(status.activeExecutionId()).isEqualTo(running.getId());
    }

    @Test
    void getTestStatus_whenNoExecutions_returnsNoExecutions() {
        when(testRepository.findById(testId)).thenReturn(Optional.of(test));
        when(executionRepository.findFirstByTest_IdOrderByStartedAtDesc(testId))
                .thenReturn(Optional.empty());

        TestStatusResponse status = service.getTestStatus(testId);

        assertThat(status.status()).isEqualTo("NO_EXECUTIONS");
        assertThat(status.activeExecutionId()).isNull();
    }

    @Test
    void listExecutions_whenTestExists_returnsMappedList() {
        Execution execution = Execution.builder()
                .id(UUID.randomUUID())
                .test(test)
                .status(ExecutionStatus.STOPPED)
                .startedAt(Instant.now())
                .finishedAt(Instant.now())
                .createdAt(Instant.now())
                .build();
        when(testRepository.findById(testId)).thenReturn(Optional.of(test));
        when(executionRepository.findByTest_IdOrderByStartedAtDesc(testId))
                .thenReturn(List.of(execution));

        List<ExecutionResponse> result = service.listExecutions(testId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(ExecutionStatus.STOPPED);
    }

    @Test
    void listExecutions_whenTestMissing_throwsNotFound() {
        when(testRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listExecutions(testId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
