package com.loadforge.testservice.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * A single metrics snapshot reported by a worker for an execution, persisted for aggregation.
 * Each row captures the request counts and average latency observed by one worker within one
 * reporting interval.
 */
@Entity
@Table(name = "metric_samples", indexes = {
        @Index(name = "idx_metric_samples_execution_id", columnList = "execution_id"),
        @Index(name = "idx_metric_samples_recorded_at", columnList = "recorded_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricSample {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "worker_id", nullable = false)
    private UUID workerId;

    @Column(name = "total_requests", nullable = false)
    private long totalRequests;

    @Column(name = "successful_requests", nullable = false)
    private long successfulRequests;

    @Column(name = "failed_requests", nullable = false)
    private long failedRequests;

    @Column(name = "average_latency_ms", nullable = false)
    private double averageLatencyMs;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;
}
