package com.loadforge.testservice.dto;

import com.loadforge.testservice.domain.HttpMethod;

import java.time.Instant;
import java.util.UUID;

/**
 * Job published to the {@code test-execution} topic instructing a worker to run a load test.
 *
 * <p>{@code assignedWorkerId} names the worker the control plane has selected to run this
 * execution. It is re-populated with a healthy worker and the job re-published when the original
 * worker fails, so the field also acts as the failover routing key.
 */
public record TestExecutionJob(
        UUID executionId,
        UUID testId,
        String targetUrl,
        HttpMethod httpMethod,
        int virtualUsers,
        int durationSeconds,
        UUID assignedWorkerId,
        Instant dispatchedAt
) {
}
