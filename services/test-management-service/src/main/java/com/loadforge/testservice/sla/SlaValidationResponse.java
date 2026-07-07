package com.loadforge.testservice.sla;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** API view of an execution's SLA validation outcome: the overall PASS/FAIL plus per-rule detail. */
public record SlaValidationResponse(
        UUID executionId,
        SlaStatus status,
        String policyName,
        Instant evaluatedAt,
        List<SlaRuleResultResponse> rules
) {
}
