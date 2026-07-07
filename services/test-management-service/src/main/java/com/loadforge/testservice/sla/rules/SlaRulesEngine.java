package com.loadforge.testservice.sla.rules;

import com.loadforge.testservice.metrics.ExecutionMetricsSummary;
import com.loadforge.testservice.sla.SlaPolicy;
import com.loadforge.testservice.sla.SlaStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs every configured {@link SlaRule} against an execution's metrics and aggregates the
 * outcomes into an overall verdict: {@link SlaStatus#PASS} only when all rules pass,
 * otherwise {@link SlaStatus#FAIL}.
 */
@Component
public class SlaRulesEngine {

    private final List<SlaRule> rules;

    public SlaRulesEngine(List<SlaRule> rules) {
        this.rules = rules;
    }

    public SlaEvaluation evaluate(ExecutionMetricsSummary metrics, SlaPolicy policy) {
        List<SlaRuleOutcome> outcomes = rules.stream()
                .map(rule -> rule.evaluate(metrics, policy))
                .toList();
        SlaStatus status = outcomes.stream().allMatch(SlaRuleOutcome::passed)
                ? SlaStatus.PASS
                : SlaStatus.FAIL;
        return new SlaEvaluation(status, outcomes);
    }
}
