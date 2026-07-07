package com.loadforge.testservice.metrics;

import java.time.Instant;

/**
 * Aggregate projection computed in the database over every {@link MetricSample} belonging to a
 * single execution. Rates and percentiles are derived from these totals in the service layer.
 */
public interface ExecutionMetricsAggregate {

    long getSampleCount();

    long getTotalRequests();

    long getSuccessfulRequests();

    long getFailedRequests();

    /** Sum of {@code averageLatencyMs * totalRequests}, used to derive a request-weighted mean latency. */
    double getWeightedLatencySum();

    /** Earliest {@code recordedAt} across the samples; {@code null} when there are no samples. */
    Instant getWindowStart();

    /** Latest {@code recordedAt} across the samples; {@code null} when there are no samples. */
    Instant getWindowEnd();
}
