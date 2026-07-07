package com.loadforge.testservice.sla.rules;

import com.loadforge.testservice.metrics.ExecutionMetricsSummary;
import com.loadforge.testservice.sla.SlaPolicy;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorRateRuleTest {

    private final ErrorRateRule rule = new ErrorRateRule();
    private final SlaPolicy policy = SlaPolicy.builder()
            .name("default").maxP95LatencyMs(300.0).maxErrorRatePercent(1.0).active(true).build();

    @Test
    void passesWhenErrorRateBelowThreshold() {
        // 50 / 10000 = 0.5%
        SlaRuleOutcome outcome = rule.evaluate(summary(10_000, 50), policy);

        assertThat(outcome.passed()).isTrue();
        assertThat(outcome.actualValue()).isEqualTo(0.5);
        assertThat(outcome.ruleName()).isEqualTo("error-rate");
        assertThat(outcome.metric()).isEqualTo("errorRatePercent");
    }

    @Test
    void failsWhenErrorRateAboveThreshold() {
        // 150 / 10000 = 1.5%
        assertThat(rule.evaluate(summary(10_000, 150), policy).passed()).isFalse();
    }

    @Test
    void failsWhenErrorRateEqualsThreshold() {
        // 100 / 10000 = 1.0%, strict "<" bound is a breach.
        SlaRuleOutcome outcome = rule.evaluate(summary(10_000, 100), policy);

        assertThat(outcome.actualValue()).isEqualTo(1.0);
        assertThat(outcome.passed()).isFalse();
    }

    @Test
    void treatsZeroRequestsAsZeroErrorRate() {
        SlaRuleOutcome outcome = rule.evaluate(summary(0, 0), policy);

        assertThat(outcome.actualValue()).isEqualTo(0.0);
        assertThat(outcome.passed()).isTrue();
    }

    private ExecutionMetricsSummary summary(long total, long failures) {
        long successful = total - failures;
        return new ExecutionMetricsSummary(
                UUID.randomUUID(), total, successful, failures,
                100.0, 100.0, 50.0, 100.0, 120.0, 5, null, null, 10.0);
    }
}
