package com.loadforge.testservice.sla.rules;

import com.loadforge.testservice.metrics.ExecutionMetricsSummary;
import com.loadforge.testservice.sla.SlaPolicy;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class P95LatencyRuleTest {

    private final P95LatencyRule rule = new P95LatencyRule();
    private final SlaPolicy policy = SlaPolicy.builder()
            .name("default").maxP95LatencyMs(300.0).maxErrorRatePercent(1.0).active(true).build();

    @Test
    void passesWhenP95BelowThreshold() {
        SlaRuleOutcome outcome = rule.evaluate(summaryWithP95(250.0), policy);

        assertThat(outcome.passed()).isTrue();
        assertThat(outcome.ruleName()).isEqualTo("p95-latency");
        assertThat(outcome.metric()).isEqualTo("p95LatencyMs");
        assertThat(outcome.comparator()).isEqualTo("<");
        assertThat(outcome.threshold()).isEqualTo(300.0);
        assertThat(outcome.actualValue()).isEqualTo(250.0);
    }

    @Test
    void failsWhenP95AboveThreshold() {
        assertThat(rule.evaluate(summaryWithP95(350.0), policy).passed()).isFalse();
    }

    @Test
    void failsWhenP95EqualsThreshold() {
        // Strict "<" bound: exactly 300ms is a breach.
        assertThat(rule.evaluate(summaryWithP95(300.0), policy).passed()).isFalse();
    }

    private ExecutionMetricsSummary summaryWithP95(double p95) {
        return new ExecutionMetricsSummary(
                UUID.randomUUID(), 1_000, 1_000, 0, 100.0, 100.0, 50.0, p95, p95, 5, null, null, 10.0);
    }
}
