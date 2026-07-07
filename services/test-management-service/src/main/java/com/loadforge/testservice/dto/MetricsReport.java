package com.loadforge.testservice.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Metrics consumed from the {@code metrics} topic reporting a worker's results for an execution.
 */
public record MetricsReport(
        UUID executionId,
        UUID workerId,
        long totalRequests,
        long successfulRequests,
        long failedRequests,
        double averageLatencyMs,
        Instant timestamp
) {
}
