package com.loadforge.testservice.dto;

import java.time.Instant;
import java.util.UUID;

public record TestStatusResponse(
        UUID testId,
        String testName,
        String status,
        UUID activeExecutionId,
        Instant since
) {
}
