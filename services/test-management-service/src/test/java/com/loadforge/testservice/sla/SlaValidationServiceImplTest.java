package com.loadforge.testservice.sla;

import com.loadforge.testservice.exception.ResourceNotFoundException;
import com.loadforge.testservice.metrics.ExecutionMetricsSummary;
import com.loadforge.testservice.metrics.MetricsService;
import com.loadforge.testservice.repository.ExecutionRepository;
import com.loadforge.testservice.sla.rules.SlaEvaluation;
import com.loadforge.testservice.sla.rules.SlaRuleOutcome;
import com.loadforge.testservice.sla.rules.SlaRulesEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlaValidationServiceImplTest {

    @Mock
    private ExecutionRepository executionRepository;
    @Mock
    private MetricsService metricsService;
    @Mock
    private SlaPolicyRepository policyRepository;
    @Mock
    private SlaValidationResultRepository resultRepository;
    @Mock
    private SlaRulesEngine rulesEngine;

    @InjectMocks
    private SlaValidationServiceImpl service;

    private UUID executionId;
    private UUID policyId;
    private SlaPolicy policy;

    @BeforeEach
    void setUp() {
        executionId = UUID.randomUUID();
        policyId = UUID.randomUUID();
        policy = SlaPolicy.builder()
                .id(policyId).name("default")
                .maxP95LatencyMs(300.0).maxErrorRatePercent(1.0).active(true).build();
    }

    @Test
    void validateExecution_whenAllRulesPass_persistsPassResult() {
        SlaEvaluation evaluation = new SlaEvaluation(SlaStatus.PASS, List.of(
                new SlaRuleOutcome("p95-latency", "p95LatencyMs", "<", 300.0, 250.0, true),
                new SlaRuleOutcome("error-rate", "errorRatePercent", "<", 1.0, 0.5, true)));
        when(executionRepository.existsById(executionId)).thenReturn(true);
        when(policyRepository.findFirstByActiveTrue()).thenReturn(Optional.of(policy));
        when(metricsService.getExecutionSummary(executionId))
                .thenReturn(ExecutionMetricsSummary.empty(executionId));
        when(rulesEngine.evaluate(any(), any())).thenReturn(evaluation);
        when(resultRepository.save(any(SlaValidationResult.class))).thenAnswer(inv -> inv.getArgument(0));

        SlaValidationResponse response = service.validateExecution(executionId);

        assertThat(response.executionId()).isEqualTo(executionId);
        assertThat(response.status()).isEqualTo(SlaStatus.PASS);
        assertThat(response.policyName()).isEqualTo("default");
        assertThat(response.rules()).extracting(SlaRuleResultResponse::rule)
                .containsExactly("p95-latency", "error-rate");
        assertThat(response.rules()).extracting(SlaRuleResultResponse::status)
                .containsExactly(SlaStatus.PASS, SlaStatus.PASS);

        ArgumentCaptor<SlaValidationResult> captor = ArgumentCaptor.forClass(SlaValidationResult.class);
        verify(resultRepository).save(captor.capture());
        SlaValidationResult persisted = captor.getValue();
        assertThat(persisted.getExecutionId()).isEqualTo(executionId);
        assertThat(persisted.getStatus()).isEqualTo(SlaStatus.PASS);
        assertThat(persisted.getRuleResults()).hasSize(2);
        assertThat(persisted.getRuleResults())
                .allSatisfy(rr -> assertThat(rr.getValidationResult()).isSameAs(persisted));
    }

    @Test
    void validateExecution_whenAnyRuleFails_persistsFailResult() {
        SlaEvaluation evaluation = new SlaEvaluation(SlaStatus.FAIL, List.of(
                new SlaRuleOutcome("p95-latency", "p95LatencyMs", "<", 300.0, 420.0, false),
                new SlaRuleOutcome("error-rate", "errorRatePercent", "<", 1.0, 0.5, true)));
        when(executionRepository.existsById(executionId)).thenReturn(true);
        when(policyRepository.findFirstByActiveTrue()).thenReturn(Optional.of(policy));
        when(metricsService.getExecutionSummary(executionId))
                .thenReturn(ExecutionMetricsSummary.empty(executionId));
        when(rulesEngine.evaluate(any(), any())).thenReturn(evaluation);
        when(resultRepository.save(any(SlaValidationResult.class))).thenAnswer(inv -> inv.getArgument(0));

        SlaValidationResponse response = service.validateExecution(executionId);

        assertThat(response.status()).isEqualTo(SlaStatus.FAIL);
        assertThat(response.rules()).extracting(SlaRuleResultResponse::status)
                .containsExactly(SlaStatus.FAIL, SlaStatus.PASS);
    }

    @Test
    void validateExecution_whenExecutionMissing_throwsAndDoesNotPersist() {
        when(executionRepository.existsById(executionId)).thenReturn(false);

        assertThatThrownBy(() -> service.validateExecution(executionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(executionId.toString());

        verify(resultRepository, never()).save(any());
    }

    @Test
    void validateExecution_whenNoActivePolicy_throwsAndDoesNotPersist() {
        when(executionRepository.existsById(executionId)).thenReturn(true);
        when(policyRepository.findFirstByActiveTrue()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateExecution(executionId))
                .isInstanceOf(IllegalStateException.class);

        verify(resultRepository, never()).save(any());
    }

    @Test
    void getLatestResult_whenPresent_mapsToResponse() {
        SlaValidationResult stored = new SlaValidationResult();
        stored.setExecutionId(executionId);
        stored.setPolicyName("default");
        stored.setStatus(SlaStatus.FAIL);
        stored.addRuleResult(SlaRuleResult.builder()
                .ruleName("p95-latency").metric("p95LatencyMs").comparator("<")
                .threshold(300.0).actualValue(420.0).status(SlaStatus.FAIL).build());
        when(resultRepository.findFirstByExecutionIdOrderByEvaluatedAtDesc(executionId))
                .thenReturn(Optional.of(stored));

        Optional<SlaValidationResponse> response = service.getLatestResult(executionId);

        assertThat(response).isPresent();
        assertThat(response.get().status()).isEqualTo(SlaStatus.FAIL);
        assertThat(response.get().rules()).hasSize(1);
        assertThat(response.get().rules().get(0).rule()).isEqualTo("p95-latency");
    }

    @Test
    void getLatestResult_whenAbsent_returnsEmpty() {
        when(resultRepository.findFirstByExecutionIdOrderByEvaluatedAtDesc(executionId))
                .thenReturn(Optional.empty());

        assertThat(service.getLatestResult(executionId)).isEmpty();
    }

    @Test
    void getActivePolicy_returnsConfiguredThresholds() {
        when(policyRepository.findFirstByActiveTrue()).thenReturn(Optional.of(policy));

        SlaPolicyResponse response = service.getActivePolicy();

        assertThat(response.id()).isEqualTo(policyId);
        assertThat(response.name()).isEqualTo("default");
        assertThat(response.maxP95LatencyMs()).isEqualTo(300.0);
        assertThat(response.maxErrorRatePercent()).isEqualTo(1.0);
        assertThat(response.active()).isTrue();
    }
}
