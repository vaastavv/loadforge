package com.loadforge.workerservice.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Liveness/status event published to the {@code worker-heartbeat} Kafka topic whenever a worker
 * registers, sends a heartbeat, or is marked {@code OFFLINE} by the heartbeat-timeout sweep.
 *
 * <p>The shape (id, status, timestamp) intentionally mirrors the control plane's
 * {@code WorkerHeartbeat} consumer contract so the test-management-service can maintain its
 * worker-liveness registry and drive execution reassignment when a worker dies.
 */
public record WorkerStatusEvent(
        UUID workerId,
        String status,
        Instant timestamp
) {
}
