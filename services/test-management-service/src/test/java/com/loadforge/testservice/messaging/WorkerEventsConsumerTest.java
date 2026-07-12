package com.loadforge.testservice.messaging;

import com.loadforge.testservice.dto.WorkerHeartbeat;
import com.loadforge.testservice.metrics.MetricsService;
import com.loadforge.testservice.scheduler.WorkerRegistry;
import com.loadforge.testservice.service.WorkerFailoverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WorkerEventsConsumerTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private WorkerRegistry workerRegistry;

    @Mock
    private WorkerFailoverService workerFailoverService;

    @InjectMocks
    private WorkerEventsConsumer consumer;

    @Test
    void onWorkerHeartbeat_whenOffline_triggersFailover() {
        UUID workerId = UUID.randomUUID();

        consumer.onWorkerHeartbeat(new WorkerHeartbeat(workerId, "OFFLINE", Instant.now()));

        verify(workerFailoverService).handleWorkerOffline(workerId);
        verify(workerRegistry, never()).register(any(), any());
    }

    @Test
    void onWorkerHeartbeat_whenActive_registersWorkerInRegistry() {
        UUID workerId = UUID.randomUUID();

        consumer.onWorkerHeartbeat(new WorkerHeartbeat(workerId, "ACTIVE", Instant.now()));

        verify(workerRegistry).register(workerId, null);
        verify(workerRegistry).updateStatus(workerId, "ACTIVE");
        verifyNoInteractions(workerFailoverService);
    }

    @Test
    void onWorkerHeartbeat_whenBusy_treatsWorkerAsAlive() {
        UUID workerId = UUID.randomUUID();

        consumer.onWorkerHeartbeat(new WorkerHeartbeat(workerId, "BUSY", Instant.now()));

        verify(workerRegistry).register(workerId, null);
        verify(workerRegistry).updateStatus(workerId, "BUSY");
        verifyNoInteractions(workerFailoverService);
    }
}
