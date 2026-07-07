package com.loadforge.testservice.sla;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SlaValidationResultRepository extends JpaRepository<SlaValidationResult, UUID> {

    /** Returns the most recent validation result for an execution, if one exists. */
    Optional<SlaValidationResult> findFirstByExecutionIdOrderByEvaluatedAtDesc(UUID executionId);
}
