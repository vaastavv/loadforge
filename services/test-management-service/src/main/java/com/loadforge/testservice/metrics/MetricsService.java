package com.loadforge.testservice.metrics;

import com.loadforge.testservice.dto.MetricsReport;

import java.util.UUID;

public interface MetricsService {

    /** Persists a metrics report received from a worker for later aggregation. */
    void ingest(MetricsReport report);

    /** Aggregates all persisted samples for an execution into a performance summary. */
    ExecutionMetricsSummary getExecutionSummary(UUID executionId);
}
