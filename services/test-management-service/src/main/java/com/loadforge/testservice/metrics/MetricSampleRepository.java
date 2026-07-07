package com.loadforge.testservice.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MetricSampleRepository extends JpaRepository<MetricSample, UUID> {

    /**
     * Computes request totals, a request-weighted latency sum, and the sampling window for an
     * execution in a single database round-trip.
     */
    @Query("""
            SELECT COUNT(m)                                              AS sampleCount,
                   COALESCE(SUM(m.totalRequests), 0)                     AS totalRequests,
                   COALESCE(SUM(m.successfulRequests), 0)                AS successfulRequests,
                   COALESCE(SUM(m.failedRequests), 0)                    AS failedRequests,
                   COALESCE(SUM(m.averageLatencyMs * m.totalRequests), 0.0) AS weightedLatencySum,
                   MIN(m.recordedAt)                                     AS windowStart,
                   MAX(m.recordedAt)                                     AS windowEnd
            FROM MetricSample m
            WHERE m.executionId = :executionId
            """)
    ExecutionMetricsAggregate aggregateByExecutionId(@Param("executionId") UUID executionId);

    /**
     * Returns the reported average latencies for an execution sorted ascending, ready for
     * nearest-rank percentile computation.
     */
    @Query("""
            SELECT m.averageLatencyMs
            FROM MetricSample m
            WHERE m.executionId = :executionId
            ORDER BY m.averageLatencyMs ASC
            """)
    List<Double> findLatencySamplesByExecutionId(@Param("executionId") UUID executionId);
}
