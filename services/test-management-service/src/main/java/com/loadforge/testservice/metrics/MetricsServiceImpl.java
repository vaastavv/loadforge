package com.loadforge.testservice.metrics;

import com.loadforge.testservice.dto.MetricsReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    private final MetricSampleRepository metricSampleRepository;

    @Override
    @Transactional
    public void ingest(MetricsReport report) {
        MetricSample sample = MetricSample.builder()
                .executionId(report.executionId())
                .workerId(report.workerId())
                .totalRequests(report.totalRequests())
                .successfulRequests(report.successfulRequests())
                .failedRequests(report.failedRequests())
                .averageLatencyMs(report.averageLatencyMs())
                .recordedAt(report.timestamp())
                .build();
        metricSampleRepository.save(sample);
        log.debug("Persisted metric sample for execution {} from worker {}",
                report.executionId(), report.workerId());
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionMetricsSummary getExecutionSummary(UUID executionId) {
        ExecutionMetricsAggregate aggregate = metricSampleRepository.aggregateByExecutionId(executionId);
        if (aggregate == null || aggregate.getSampleCount() == 0) {
            return ExecutionMetricsSummary.empty(executionId);
        }

        List<Double> latencies = metricSampleRepository.findLatencySamplesByExecutionId(executionId);

        double windowSeconds = windowSeconds(aggregate.getWindowStart(), aggregate.getWindowEnd());
        // A zero-length window (single sample or identical timestamps) collapses to one second so
        // rates remain finite instead of dividing by zero.
        double effectiveWindow = windowSeconds > 0.0 ? windowSeconds : 1.0;

        long totalRequests = aggregate.getTotalRequests();
        long successfulRequests = aggregate.getSuccessfulRequests();
        long failures = aggregate.getFailedRequests();

        double requestsPerSecond = totalRequests / effectiveWindow;
        double throughput = successfulRequests / effectiveWindow;
        double averageLatencyMs = totalRequests > 0
                ? aggregate.getWeightedLatencySum() / totalRequests
                : mean(latencies);

        return new ExecutionMetricsSummary(
                executionId,
                totalRequests,
                successfulRequests,
                failures,
                requestsPerSecond,
                throughput,
                averageLatencyMs,
                percentile(latencies, 95.0),
                percentile(latencies, 99.0),
                aggregate.getSampleCount(),
                aggregate.getWindowStart(),
                aggregate.getWindowEnd(),
                windowSeconds);
    }

    private static double windowSeconds(Instant start, Instant end) {
        if (start == null || end == null) {
            return 0.0;
        }
        return Duration.between(start, end).toNanos() / 1_000_000_000.0;
    }

    private static double mean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * Nearest-rank percentile over latency samples already sorted ascending. The samples are the
     * per-report average latencies, so the result describes the distribution of reported latencies
     * rather than of individual requests.
     */
    private static double percentile(List<Double> sortedAscending, double percentile) {
        if (sortedAscending.isEmpty()) {
            return 0.0;
        }
        int size = sortedAscending.size();
        int rank = (int) Math.ceil(percentile / 100.0 * size);
        rank = Math.max(1, Math.min(rank, size));
        return sortedAscending.get(rank - 1);
    }
}
