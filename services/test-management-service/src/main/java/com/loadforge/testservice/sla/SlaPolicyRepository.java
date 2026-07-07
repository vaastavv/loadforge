package com.loadforge.testservice.sla;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, UUID> {

    /** Returns the active default policy used to validate executions. */
    Optional<SlaPolicy> findFirstByActiveTrue();
}
