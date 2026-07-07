package com.loadforge.testservice.sla.rules;

import com.loadforge.testservice.metrics.ExecutionMetricsSummary;
import com.loadforge.testservice.sla.SlaPolicy;

/**
 * A single SLA rule that inspects an execution's aggregated metrics against a policy
 * threshold. Implementations are discovered as Spring beans and run by {@link SlaRulesEngine},
 * so new SLA criteria can be added simply by contributing another {@code SlaRule} bean.
 */
public interface SlaRule {

    /** Evaluates this rule for the given metrics and policy. */
    SlaRuleOutcome evaluate(ExecutionMetricsSummary metrics, SlaPolicy policy);
}
