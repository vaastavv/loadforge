package com.loadforge.testservice.sla.rules;

import com.loadforge.testservice.sla.SlaStatus;

import java.util.List;

/**
 * The aggregate result produced by {@link SlaRulesEngine}: the overall {@link SlaStatus}
 * plus the individual {@link SlaRuleOutcome} of every rule that was evaluated.
 */
public record SlaEvaluation(SlaStatus status, List<SlaRuleOutcome> outcomes) {
}
