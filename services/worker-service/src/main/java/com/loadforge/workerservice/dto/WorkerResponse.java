package com.loadforge.workerservice.dto;

import com.loadforge.workerservice.domain.WorkerStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkerResponse(
        UUID id,
        String hostname,
        WorkerStatus status,
        Instant lastHeartbeat,
        Instant registeredAt
) {
}
