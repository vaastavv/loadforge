package com.loadforge.testservice.dto;

import com.loadforge.testservice.domain.ExecutionStatus;

import java.time.Instant;
import java.util.UUID;

public record ExecutionResponse(
        UUID id,
        UUID testId,
        ExecutionStatus status,
        Instant startedAt,
        Instant finishedAt,
        String errorMessage,
        Instant createdAt
) {
}
