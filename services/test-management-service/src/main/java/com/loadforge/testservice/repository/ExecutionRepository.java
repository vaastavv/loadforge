package com.loadforge.testservice.repository;

import com.loadforge.testservice.domain.Execution;
import com.loadforge.testservice.domain.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<Execution, UUID> {

    List<Execution> findAllByOrderByStartedAtDesc();

    List<Execution> findByTest_IdOrderByStartedAtDesc(UUID testId);

    Optional<Execution> findFirstByTest_IdOrderByStartedAtDesc(UUID testId);

    Optional<Execution> findFirstByTest_IdAndStatusOrderByStartedAtDesc(UUID testId, ExecutionStatus status);

    boolean existsByTest_IdAndStatus(UUID testId, ExecutionStatus status);

    List<Execution> findByAssignedWorkerIdAndStatus(UUID assignedWorkerId, ExecutionStatus status);

    long countByAssignedWorkerIdAndStatus(UUID assignedWorkerId, ExecutionStatus status);

    long countByStatus(ExecutionStatus status);
}
