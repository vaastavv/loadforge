package com.loadforge.testservice.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Heartbeat consumed from the {@code worker-heartbeat} topic reporting a worker's liveness and status.
 */
public record WorkerHeartbeat(
        UUID workerId,
        String hostname,
        String status,
        Instant timestamp
) {

    /** Backward-compatible view for heartbeats published before the hostname field existed. */
    public WorkerHeartbeat(UUID workerId, String status, Instant timestamp) {
        this(workerId, null, status, timestamp);
    }
}
