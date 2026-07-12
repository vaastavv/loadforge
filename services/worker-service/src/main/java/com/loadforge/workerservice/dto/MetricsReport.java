package com.loadforge.workerservice.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Cumulative metrics this worker publishes to the {@code metrics} topic while running an execution.
 *
 * <p>The field names and shape intentionally mirror the control plane's consumer DTO so the
 * headerless JSON payload is deserialized there by inferred type (no {@code __TypeId__} header).
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
