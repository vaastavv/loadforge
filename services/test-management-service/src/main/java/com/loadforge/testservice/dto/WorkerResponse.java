package com.loadforge.testservice.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkerResponse(
        UUID id,
        String hostname,
        String status,
        Instant lastHeartbeat,
        Instant registeredAt
) {
}
