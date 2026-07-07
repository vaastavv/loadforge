package com.loadforge.testservice.sla.rules;

/**
 * The immutable outcome of evaluating a single SLA rule.
 *
 * @param ruleName    stable identifier of the rule (e.g. {@code "p95-latency"})
 * @param metric      the metric the rule inspects (e.g. {@code "p95LatencyMs"})
 * @param comparator  the comparison applied against the threshold (e.g. {@code "<"})
 * @param threshold   the configured threshold the metric is compared to
 * @param actualValue the measured value taken from the execution metrics
 * @param passed      {@code true} when the measured value satisfies the threshold
 */
public record SlaRuleOutcome(
        String ruleName,
        String metric,
        String comparator,
        double threshold,
        double actualValue,
        boolean passed
) {
}
