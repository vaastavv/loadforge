package com.loadforge.testservice.metrics;

import com.loadforge.testservice.dto.MetricsReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsServiceImplTest {

    @Mock
    private MetricSampleRepository repository;

    @InjectMocks
    private MetricsServiceImpl service;

    private final UUID executionId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void ingestPersistsSampleFromReport() {
        UUID workerId = UUID.fromString("00000000-0000-0000-0000-000000000009");
        Instant recordedAt = Instant.parse("2026-01-01T00:00:00Z");
        MetricsReport report = new MetricsReport(executionId, workerId, 100, 95, 5, 42.0, recordedAt);

        service.ingest(report);

        ArgumentCaptor<MetricSample> captor = ArgumentCaptor.forClass(MetricSample.class);
        verify(repository).save(captor.capture());
        MetricSample saved = captor.getValue();
        assertThat(saved.getExecutionId()).isEqualTo(executionId);
        assertThat(saved.getWorkerId()).isEqualTo(workerId);
        assertThat(saved.getTotalRequests()).isEqualTo(100);
        assertThat(saved.getSuccessfulRequests()).isEqualTo(95);
        assertThat(saved.getFailedRequests()).isEqualTo(5);
        assertThat(saved.getAverageLatencyMs()).isEqualTo(42.0);
        assertThat(saved.getRecordedAt()).isEqualTo(recordedAt);
    }

    @Test
    void getExecutionSummaryReturnsEmptyWhenNoSamples() {
        when(repository.aggregateByExecutionId(executionId))
                .thenReturn(new Aggregate(0, 0, 0, 0, 0.0, null, null));

        ExecutionMetricsSummary summary = service.getExecutionSummary(executionId);

        assertThat(summary.executionId()).isEqualTo(executionId);
        assertThat(summary.totalRequests()).isZero();
        assertThat(summary.requestsPerSecond()).isZero();
        assertThat(summary.throughput()).isZero();
        assertThat(summary.p95LatencyMs()).isZero();
        assertThat(summary.p99LatencyMs()).isZero();
        assertThat(summary.sampleCount()).isZero();
        assertThat(summary.windowStart()).isNull();
    }

    @Test
    void getExecutionSummaryComputesRatesThroughputFailuresAndWeightedLatency() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = start.plus(10, ChronoUnit.SECONDS);
        // 10,000 requests over a 10s window => 1000 req/s; 9,000 successful => 900 throughput.
        when(repository.aggregateByExecutionId(executionId))
                .thenReturn(new Aggregate(4, 10_000, 9_000, 1_000, 10_000 * 50.0, start, end));
        when(repository.findLatencySamplesByExecutionId(executionId))
                .thenReturn(List.of(10.0, 20.0, 50.0, 120.0));

        ExecutionMetricsSummary summary = service.getExecutionSummary(executionId);

        assertThat(summary.totalRequests()).isEqualTo(10_000);
        assertThat(summary.successfulRequests()).isEqualTo(9_000);
        assertThat(summary.failures()).isEqualTo(1_000);
        assertThat(summary.windowSeconds()).isEqualTo(10.0);
        assertThat(summary.requestsPerSecond()).isCloseTo(1_000.0, within(1e-9));
        assertThat(summary.throughput()).isCloseTo(900.0, within(1e-9));
        assertThat(summary.averageLatencyMs()).isCloseTo(50.0, within(1e-9));
        assertThat(summary.sampleCount()).isEqualTo(4);
    }

    @Test
    void getExecutionSummaryComputesNearestRankPercentiles() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = start.plus(1, ChronoUnit.SECONDS);
        List<Double> latencies = List.of(10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0);
        when(repository.aggregateByExecutionId(executionId))
                .thenReturn(new Aggregate(10, 1_000, 1_000, 0, 1_000 * 55.0, start, end));
        when(repository.findLatencySamplesByExecutionId(executionId)).thenReturn(latencies);

        ExecutionMetricsSummary summary = service.getExecutionSummary(executionId);

        // Nearest-rank over 10 samples: p95 -> ceil(9.5)=10th, p99 -> ceil(9.9)=10th.
        assertThat(summary.p95LatencyMs()).isEqualTo(100.0);
        assertThat(summary.p99LatencyMs()).isEqualTo(100.0);
        assertThat(summary.averageLatencyMs()).isCloseTo(55.0, within(1e-9));
    }

    @Test
    void getExecutionSummaryCollapsesZeroLengthWindowToOneSecond() {
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        when(repository.aggregateByExecutionId(executionId))
                .thenReturn(new Aggregate(1, 500, 500, 0, 500 * 12.0, instant, instant));
        when(repository.findLatencySamplesByExecutionId(executionId)).thenReturn(List.of(12.0));

        ExecutionMetricsSummary summary = service.getExecutionSummary(executionId);

        assertThat(summary.windowSeconds()).isZero();
        assertThat(summary.requestsPerSecond()).isCloseTo(500.0, within(1e-9));
        assertThat(summary.throughput()).isCloseTo(500.0, within(1e-9));
    }

    /** Simple stand-in for the Spring Data interface projection. */
    private record Aggregate(
            long sampleCount,
            long totalRequests,
            long successfulRequests,
            long failedRequests,
            double weightedLatencySum,
            Instant windowStart,
            Instant windowEnd
    ) implements ExecutionMetricsAggregate {

        @Override
        public long getSampleCount() {
            return sampleCount;
        }

        @Override
        public long getTotalRequests() {
            return totalRequests;
        }

        @Override
        public long getSuccessfulRequests() {
            return successfulRequests;
        }

        @Override
        public long getFailedRequests() {
            return failedRequests;
        }

        @Override
        public double getWeightedLatencySum() {
            return weightedLatencySum;
        }

        @Override
        public Instant getWindowStart() {
            return windowStart;
        }

        @Override
        public Instant getWindowEnd() {
            return windowEnd;
        }
    }
}
