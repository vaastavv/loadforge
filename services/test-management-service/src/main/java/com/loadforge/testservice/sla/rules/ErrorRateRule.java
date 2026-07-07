package com.loadforge.testservice.sla.rules;

import com.loadforge.testservice.metrics.ExecutionMetricsSummary;
import com.loadforge.testservice.sla.SlaPolicy;
import org.springframework.stereotype.Component;

/** Passes when the execution's error rate is strictly below the policy threshold. */
@Component
public class ErrorRateRule implements SlaRule {

    static final String RULE_NAME = "error-rate";
    static final String METRIC = "errorRatePercent";
    static final String COMPARATOR = "<";

    @Override
    public SlaRuleOutcome evaluate(ExecutionMetricsSummary metrics, SlaPolicy policy) {
        double actual = errorRatePercent(metrics);
        double threshold = policy.getMaxErrorRatePercent();
        return new SlaRuleOutcome(RULE_NAME, METRIC, COMPARATOR, threshold, actual, actual < threshold);
    }

    private double errorRatePercent(ExecutionMetricsSummary metrics) {
        long total = metrics.totalRequests();
        if (total == 0) {
            return 0.0;
        }
        return metrics.failures() * 100.0 / total;
    }
}
