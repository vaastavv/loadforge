package com.loadforge.testservice.sla.rules;

import com.loadforge.testservice.metrics.ExecutionMetricsSummary;
import com.loadforge.testservice.sla.SlaPolicy;
import com.loadforge.testservice.sla.SlaStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SlaRulesEngineTest {

    private final SlaRulesEngine engine =
            new SlaRulesEngine(List.of(new P95LatencyRule(), new ErrorRateRule()));
    private final SlaPolicy policy = SlaPolicy.builder()
            .name("default").maxP95LatencyMs(300.0).maxErrorRatePercent(1.0).active(true).build();

    @Test
    void passesWhenAllRulesWithinThreshold() {
        // p95 250 < 300, error rate 0.5% < 1%
        SlaEvaluation evaluation = engine.evaluate(summary(250.0, 10_000, 50), policy);

        assertThat(evaluation.status()).isEqualTo(SlaStatus.PASS);
        assertThat(evaluation.outcomes()).hasSize(2);
        assertThat(evaluation.outcomes()).allMatch(SlaRuleOutcome::passed);
    }

    @Test
    void failsWhenLatencyBreached() {
        // p95 420 >= 300
        SlaEvaluation evaluation = engine.evaluate(summary(420.0, 10_000, 50), policy);

        assertThat(evaluation.status()).isEqualTo(SlaStatus.FAIL);
    }

    @Test
    void failsWhenErrorRateBreached() {
        // error rate 2% >= 1%
        SlaEvaluation evaluation = engine.evaluate(summary(250.0, 10_000, 200), policy);

        assertThat(evaluation.status()).isEqualTo(SlaStatus.FAIL);
    }

    @Test
    void failsWhenBothBreached() {
        SlaEvaluation evaluation = engine.evaluate(summary(420.0, 10_000, 200), policy);

        assertThat(evaluation.status()).isEqualTo(SlaStatus.FAIL);
        assertThat(evaluation.outcomes()).noneMatch(SlaRuleOutcome::passed);
    }

    private ExecutionMetricsSummary summary(double p95, long total, long failures) {
        long successful = total - failures;
        return new ExecutionMetricsSummary(
                UUID.randomUUID(), total, successful, failures,
                100.0, 100.0, 50.0, p95, p95, 5, null, null, 10.0);
    }
}
