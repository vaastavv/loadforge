package com.loadforge.testservice.sla.rules;

import com.loadforge.testservice.metrics.ExecutionMetricsSummary;
import com.loadforge.testservice.sla.SlaPolicy;
import org.springframework.stereotype.Component;

/** Passes when the execution's p95 latency is strictly below the policy threshold. */
@Component
public class P95LatencyRule implements SlaRule {

    static final String RULE_NAME = "p95-latency";
    static final String METRIC = "p95LatencyMs";
    static final String COMPARATOR = "<";

    @Override
    public SlaRuleOutcome evaluate(ExecutionMetricsSummary metrics, SlaPolicy policy) {
        double actual = metrics.p95LatencyMs();
        double threshold = policy.getMaxP95LatencyMs();
        return new SlaRuleOutcome(RULE_NAME, METRIC, COMPARATOR, threshold, actual, actual < threshold);
    }
}
