package com.loadforge.testservice.sla;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The persisted outcome of validating a single execution against an {@link SlaPolicy}.
 * Holds the overall {@link SlaStatus} plus one {@link SlaRuleResult} per rule evaluated.
 */
@Entity
@Table(name = "sla_validation_results")
@Getter
@Setter
@NoArgsConstructor
public class SlaValidationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "policy_name", nullable = false, length = 100)
    private String policyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private SlaStatus status;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @OneToMany(mappedBy = "validationResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SlaRuleResult> ruleResults = new ArrayList<>();

    /** Adds a rule result and maintains the bidirectional association. */
    public void addRuleResult(SlaRuleResult ruleResult) {
        ruleResults.add(ruleResult);
        ruleResult.setValidationResult(this);
    }
}
