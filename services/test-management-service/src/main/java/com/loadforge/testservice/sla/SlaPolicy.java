package com.loadforge.testservice.sla;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * A named set of SLA thresholds that executions are validated against. The seeded
 * {@code default} policy encodes the module inputs: p95 &lt; 300ms and error rate &lt; 1%.
 */
@Entity
@Table(name = "sla_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /** Maximum tolerated 95th percentile latency, in milliseconds (exclusive bound). */
    @Column(name = "max_p95_latency_ms", nullable = false)
    private double maxP95LatencyMs;

    /** Maximum tolerated error rate, as a percentage from 0 to 100 (exclusive bound). */
    @Column(name = "max_error_rate_percent", nullable = false)
    private double maxErrorRatePercent;

    /** Whether this policy is the active default used to validate executions. */
    @Column(name = "active", nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
