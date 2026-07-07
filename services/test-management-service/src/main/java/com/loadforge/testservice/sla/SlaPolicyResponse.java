package com.loadforge.testservice.sla;

import java.util.UUID;

/** API view of an SLA policy's configured thresholds. */
public record SlaPolicyResponse(
        UUID id,
        String name,
        double maxP95LatencyMs,
        double maxErrorRatePercent,
        boolean active
) {
}
