package com.loadforge.testservice.metrics;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregated performance metrics for a single execution.
 *
 * @param executionId        the execution these metrics belong to
 * @param totalRequests      total requests reported across all workers
 * @param successfulRequests total successful requests reported across all workers
 * @param failures           total failed requests reported across all workers
 * @param requestsPerSecond  total requests divided by the sampling window (seconds)
 * @param throughput         successful requests divided by the sampling window (seconds)
 * @param averageLatencyMs   request-weighted mean of the reported average latencies
 * @param p95LatencyMs       95th percentile (nearest-rank) of the reported average latencies
 * @param p99LatencyMs       99th percentile (nearest-rank) of the reported average latencies
 * @param sampleCount        number of persisted samples aggregated
 * @param windowStart        earliest sample timestamp, or {@code null} when no samples exist
 * @param windowEnd          latest sample timestamp, or {@code null} when no samples exist
 * @param windowSeconds      elapsed seconds between {@code windowStart} and {@code windowEnd}
 */
public record ExecutionMetricsSummary(
        UUID executionId,
        long totalRequests,
        long successfulRequests,
        long failures,
        double requestsPerSecond,
        double throughput,
        double averageLatencyMs,
        double p95LatencyMs,
        double p99LatencyMs,
        long sampleCount,
        Instant windowStart,
        Instant windowEnd,
        double windowSeconds
) {

    /** An all-zero summary for an execution that has no persisted samples yet. */
    public static ExecutionMetricsSummary empty(UUID executionId) {
        return new ExecutionMetricsSummary(
                executionId, 0L, 0L, 0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0L, null, null, 0.0);
    }
}
