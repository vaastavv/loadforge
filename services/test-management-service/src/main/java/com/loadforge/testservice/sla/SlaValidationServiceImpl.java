package com.loadforge.testservice.sla;

import com.loadforge.testservice.exception.ResourceNotFoundException;
import com.loadforge.testservice.metrics.ExecutionMetricsSummary;
import com.loadforge.testservice.metrics.MetricsService;
import com.loadforge.testservice.repository.ExecutionRepository;
import com.loadforge.testservice.sla.rules.SlaEvaluation;
import com.loadforge.testservice.sla.rules.SlaRuleOutcome;
import com.loadforge.testservice.sla.rules.SlaRulesEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Evaluates an execution's aggregated metrics against the active {@link SlaPolicy} using the
 * {@link SlaRulesEngine}, persists the verdict, and exposes it for retrieval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlaValidationServiceImpl implements SlaValidationService {

    private final ExecutionRepository executionRepository;
    private final MetricsService metricsService;
    private final SlaPolicyRepository policyRepository;
    private final SlaValidationResultRepository resultRepository;
    private final SlaRulesEngine rulesEngine;

    @Override
    @Transactional
    public SlaValidationResponse validateExecution(UUID executionId) {
        if (!executionRepository.existsById(executionId)) {
            throw new ResourceNotFoundException("Execution not found: %s".formatted(executionId));
        }

        SlaPolicy policy = activePolicy();
        ExecutionMetricsSummary metrics = metricsService.getExecutionSummary(executionId);
        SlaEvaluation evaluation = rulesEngine.evaluate(metrics, policy);

        SlaValidationResult result = new SlaValidationResult();
        result.setExecutionId(executionId);
        result.setPolicyName(policy.getName());
        result.setStatus(evaluation.status());
        result.setEvaluatedAt(Instant.now());
        for (SlaRuleOutcome outcome : evaluation.outcomes()) {
            result.addRuleResult(SlaRuleResult.builder()
                    .ruleName(outcome.ruleName())
                    .metric(outcome.metric())
                    .comparator(outcome.comparator())
                    .threshold(outcome.threshold())
                    .actualValue(outcome.actualValue())
                    .status(outcome.passed() ? SlaStatus.PASS : SlaStatus.FAIL)
                    .build());
        }

        SlaValidationResult saved = resultRepository.save(result);
        log.info("SLA validation for execution {}: {}", executionId, saved.getStatus());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SlaValidationResponse> getLatestResult(UUID executionId) {
        return resultRepository.findFirstByExecutionIdOrderByEvaluatedAtDesc(executionId)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SlaPolicyResponse getActivePolicy() {
        SlaPolicy policy = activePolicy();
        return new SlaPolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getMaxP95LatencyMs(),
                policy.getMaxErrorRatePercent(),
                policy.isActive());
    }

    private SlaPolicy activePolicy() {
        return policyRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active SLA policy configured"));
    }

    private SlaValidationResponse toResponse(SlaValidationResult result) {
        List<SlaRuleResultResponse> rules = result.getRuleResults().stream()
                .map(rule -> new SlaRuleResultResponse(
                        rule.getRuleName(),
                        rule.getMetric(),
                        rule.getComparator(),
                        rule.getThreshold(),
                        rule.getActualValue(),
                        rule.getStatus()))
                .toList();
        return new SlaValidationResponse(
                result.getExecutionId(),
                result.getStatus(),
                result.getPolicyName(),
                result.getEvaluatedAt(),
                rules);
    }
}
