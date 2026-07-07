package com.loadforge.testservice.sla;

/** API view of a single SLA rule outcome within a validation result. */
public record SlaRuleResultResponse(
        String rule,
        String metric,
        String comparator,
        double threshold,
        double actualValue,
        SlaStatus status
) {
}
