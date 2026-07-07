package com.loadforge.testservice.service;

import com.loadforge.testservice.domain.ExecutionStatus;
import com.loadforge.testservice.repository.ExecutionRepository;
import com.loadforge.testservice.scheduler.WorkerRegistry;
import com.loadforge.testservice.scheduler.model.WorkerNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkerAssignmentServiceTest {

    @Mock
    private WorkerRegistry workerRegistry;

    @Mock
    private ExecutionRepository executionRepository;

    @InjectMocks
    private WorkerAssignmentService service;

    private static final UUID WORKER_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID WORKER_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void selectWorker_picksLeastBusyHealthyWorker() {
        when(workerRegistry.healthyWorkers()).thenReturn(List.of(
                new WorkerNode(WORKER_1, true, 0),
                new WorkerNode(WORKER_2, true, 0)));
        when(executionRepository.countByAssignedWorkerIdAndStatus(WORKER_1, ExecutionStatus.RUNNING)).thenReturn(3L);
        when(executionRepository.countByAssignedWorkerIdAndStatus(WORKER_2, ExecutionStatus.RUNNING)).thenReturn(1L);

        Optional<UUID> chosen = service.selectWorker();

        assertThat(chosen).contains(WORKER_2);
    }

    @Test
    void selectWorker_breaksTiesByWorkerId() {
        when(workerRegistry.healthyWorkers()).thenReturn(List.of(
                new WorkerNode(WORKER_2, true, 0),
                new WorkerNode(WORKER_1, true, 0)));
        when(executionRepository.countByAssignedWorkerIdAndStatus(WORKER_1, ExecutionStatus.RUNNING)).thenReturn(2L);
        when(executionRepository.countByAssignedWorkerIdAndStatus(WORKER_2, ExecutionStatus.RUNNING)).thenReturn(2L);

        Optional<UUID> chosen = service.selectWorker();

        assertThat(chosen).contains(WORKER_1);
    }

    @Test
    void selectWorker_excludesGivenWorkers() {
        when(workerRegistry.healthyWorkers()).thenReturn(List.of(
                new WorkerNode(WORKER_1, true, 0),
                new WorkerNode(WORKER_2, true, 0)));

        Optional<UUID> chosen = service.selectWorker(Set.of(WORKER_1));

        assertThat(chosen).contains(WORKER_2);
    }

    @Test
    void selectWorker_whenNoHealthyWorkers_returnsEmpty() {
        when(workerRegistry.healthyWorkers()).thenReturn(List.of());

        assertThat(service.selectWorker()).isEmpty();
        verifyNoInteractions(executionRepository);
    }

    @Test
    void selectWorker_whenAllCandidatesExcluded_returnsEmpty() {
        when(workerRegistry.healthyWorkers()).thenReturn(List.of(new WorkerNode(WORKER_1, true, 0)));

        assertThat(service.selectWorker(Set.of(WORKER_1))).isEmpty();
        verifyNoInteractions(executionRepository);
    }
}
