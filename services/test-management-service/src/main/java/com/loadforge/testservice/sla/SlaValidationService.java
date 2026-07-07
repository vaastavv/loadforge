package com.loadforge.testservice.sla;

import java.util.Optional;
import java.util.UUID;

public interface SlaValidationService {

    /** Evaluates the execution's metrics against the active SLA policy and persists the result. */
    SlaValidationResponse validateExecution(UUID executionId);

    /** Returns the most recent SLA validation result for an execution, if any. */
    Optional<SlaValidationResponse> getLatestResult(UUID executionId);

    /** Returns the currently active SLA policy and its thresholds. */
    SlaPolicyResponse getActivePolicy();
}
