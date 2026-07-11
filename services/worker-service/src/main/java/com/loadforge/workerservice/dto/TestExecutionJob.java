package com.loadforge.workerservice.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Job published by the control plane for a worker to execute.
 */
public record TestExecutionJob(
        UUID executionId,
        UUID testId,
        String targetUrl,
        String httpMethod,
        int virtualUsers,
        int durationSeconds,
        UUID assignedWorkerId,
        Instant dispatchedAt
) {
}
