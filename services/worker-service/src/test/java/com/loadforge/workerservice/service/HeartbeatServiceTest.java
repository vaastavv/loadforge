package com.loadforge.workerservice.service;

import com.loadforge.workerservice.domain.Worker;
import com.loadforge.workerservice.domain.WorkerStatus;
import com.loadforge.workerservice.dto.HeartbeatRequest;
import com.loadforge.workerservice.dto.RegisterWorkerRequest;
import com.loadforge.workerservice.dto.WorkerResponse;
import com.loadforge.workerservice.messaging.WorkerEventPublisher;
import com.loadforge.workerservice.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeartbeatServiceTest {

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private WorkerEventPublisher eventPublisher;

    private HeartbeatService service;

    @BeforeEach
    void setUp() {
        service = new HeartbeatService(workerRepository, eventPublisher, 30L);
    }

    @Test
    void register_savesActiveWorker_andPublishesActiveEvent() {
        UUID workerId = UUID.randomUUID();
        when(workerRepository.save(any(Worker.class))).thenAnswer(invocation -> {
            Worker worker = invocation.getArgument(0);
            worker.setId(workerId);
            return worker;
        });

        WorkerResponse response = service.register(new RegisterWorkerRequest("worker-01"));

        assertThat(response.status()).isEqualTo(WorkerStatus.ACTIVE);
        verify(eventPublisher).publishStatus(workerId, WorkerStatus.ACTIVE);
    }

    @Test
    void heartbeat_updatesStatus_andPublishesThatStatus() {
        UUID workerId = UUID.randomUUID();
        Worker existing = Worker.builder()
                .id(workerId)
                .hostname("worker-01")
                .status(WorkerStatus.ACTIVE)
                .lastHeartbeat(Instant.now().minusSeconds(5))
                .build();
        when(workerRepository.findById(workerId)).thenReturn(Optional.of(existing));
        when(workerRepository.save(any(Worker.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkerResponse response = service.heartbeat(new HeartbeatRequest(workerId, WorkerStatus.BUSY));

        assertThat(response.status()).isEqualTo(WorkerStatus.BUSY);
        verify(eventPublisher).publishStatus(workerId, WorkerStatus.BUSY);
    }

    @Test
    void heartbeat_whenWorkerMissing_throws_andPublishesNothing() {
        UUID workerId = UUID.randomUUID();
        when(workerRepository.findById(workerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.heartbeat(new HeartbeatRequest(workerId, WorkerStatus.ACTIVE)))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void markStaleWorkersOffline_refreshesActiveWorkersAndPublishesStatus() {
        UUID workerId = UUID.randomUUID();
        Worker active = Worker.builder()
                .id(workerId)
                .hostname("w1")
                .status(WorkerStatus.ACTIVE)
                .lastHeartbeat(Instant.now().minusSeconds(5))
                .build();
        when(workerRepository.findAll()).thenReturn(List.of(active));
        when(workerRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.markStaleWorkersOffline();

        assertThat(active.getLastHeartbeat()).isNotNull();
        assertThat(active.getStatus()).isEqualTo(WorkerStatus.ACTIVE);
        verify(workerRepository).saveAll(List.of(active));
        verify(eventPublisher).publishStatus(workerId, WorkerStatus.ACTIVE);
    }

    @Test
    void markStaleWorkersOffline_marksAndPublishesOfflinePerStaleWorker() {
        Worker stale1 = Worker.builder()
                .id(UUID.randomUUID())
                .hostname("w1")
                .status(WorkerStatus.ACTIVE)
                .lastHeartbeat(Instant.now().minusSeconds(120))
                .build();
        Worker stale2 = Worker.builder()
                .id(UUID.randomUUID())
                .hostname("w2")
                .status(WorkerStatus.BUSY)
                .lastHeartbeat(Instant.now().minusSeconds(120))
                .build();
        when(workerRepository.findByStatusNotAndLastHeartbeatBefore(eq(WorkerStatus.OFFLINE), any(Instant.class)))
                .thenReturn(List.of(stale1, stale2));

        service.markStaleWorkersOffline();

        assertThat(stale1.getStatus()).isEqualTo(WorkerStatus.OFFLINE);
        assertThat(stale2.getStatus()).isEqualTo(WorkerStatus.OFFLINE);
        verify(workerRepository).saveAll(List.of(stale1, stale2));
        verify(eventPublisher).publishStatus(stale1.getId(), WorkerStatus.OFFLINE);
        verify(eventPublisher).publishStatus(stale2.getId(), WorkerStatus.OFFLINE);
    }

    @Test
    void markStaleWorkersOffline_whenNoneStale_doesNothing() {
        when(workerRepository.findByStatusNotAndLastHeartbeatBefore(eq(WorkerStatus.OFFLINE), any(Instant.class)))
                .thenReturn(List.of());

        service.markStaleWorkersOffline();

        verify(workerRepository, never()).saveAll(any());
        verifyNoInteractions(eventPublisher);
    }
}
