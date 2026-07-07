package com.loadforge.testservice.sla;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * The outcome of a single SLA rule within an {@link SlaValidationResult}: the metric it
 * inspected, the threshold it was compared against, the measured value, and its verdict.
 */
@Entity
@Table(name = "sla_rule_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaRuleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "validation_result_id", nullable = false)
    private SlaValidationResult validationResult;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "metric", nullable = false, length = 100)
    private String metric;

    @Column(name = "comparator", nullable = false, length = 10)
    private String comparator;

    @Column(name = "threshold", nullable = false)
    private double threshold;

    @Column(name = "actual_value", nullable = false)
    private double actualValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private SlaStatus status;
}
