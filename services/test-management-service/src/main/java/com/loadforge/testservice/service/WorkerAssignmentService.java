package com.loadforge.testservice.service;

import com.loadforge.testservice.domain.ExecutionStatus;
import com.loadforge.testservice.repository.ExecutionRepository;
import com.loadforge.testservice.scheduler.WorkerRegistry;
import com.loadforge.testservice.scheduler.model.WorkerNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Selects a live worker to run an execution, both when a test is first started and when an
 * execution must be moved off a failed worker.
 *
 * <p>Liveness comes from the {@link WorkerRegistry} (fed by worker heartbeat events); among the
 * healthy workers the least-busy one is chosen, measured by its current number of {@code RUNNING}
 * executions, with worker id as a deterministic tie-breaker.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerAssignmentService {

    private final WorkerRegistry workerRegistry;
    private final ExecutionRepository executionRepository;

    /** Selects the least-busy healthy worker, or empty if none are currently available. */
    public Optional<UUID> selectWorker() {
        return selectWorker(Set.of());
    }

    /**
     * Selects the least-busy healthy worker excluding the given ids (e.g. a failed worker being
     * evacuated), or empty if no eligible worker is available.
     */
    public Optional<UUID> selectWorker(Set<UUID> excluded) {
        List<UUID> candidates = workerRegistry.healthyWorkers().stream()
                .map(WorkerNode::getWorkerId)
                .filter(id -> !excluded.contains(id))
                .toList();

        if (candidates.isEmpty()) {
            log.warn("No healthy workers available for assignment (excluded={})", excluded);
            return Optional.empty();
        }

        return candidates.stream()
                .min(Comparator
                        .comparingLong((UUID id) ->
                                executionRepository.countByAssignedWorkerIdAndStatus(id, ExecutionStatus.RUNNING))
                        .thenComparing(Comparator.naturalOrder()));
    }
}
