package com.loadforge.testservice.service;

import com.loadforge.testservice.domain.Execution;
import com.loadforge.testservice.domain.ExecutionStatus;
import com.loadforge.testservice.domain.Test;
import com.loadforge.testservice.dto.TestExecutionJob;
import com.loadforge.testservice.messaging.TestExecutionProducer;
import com.loadforge.testservice.repository.ExecutionRepository;
import com.loadforge.testservice.scheduler.WorkerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Recovers executions stranded on a worker that has died.
 *
 * <p>Recovery flow: when the worker-service reports a worker {@code OFFLINE} (after the 30s
 * heartbeat timeout), this service is invoked with that worker's id and:
 * <ol>
 *   <li>marks the worker unhealthy in the {@link WorkerRegistry} so it is no longer schedulable;</li>
 *   <li>loads every {@code RUNNING} execution assigned to the dead worker;</li>
 *   <li>for each, selects a different healthy worker via {@link WorkerAssignmentService},
 *       repoints the execution's {@code assignedWorkerId}, and re-publishes the job so the new
 *       worker picks it up;</li>
 *   <li>if no healthy worker is available, fails the execution with a descriptive error rather
 *       than leaving it silently orphaned.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerFailoverService {

    private final WorkerRegistry workerRegistry;
    private final ExecutionRepository executionRepository;
    private final WorkerAssignmentService workerAssignmentService;
    private final TestExecutionProducer testExecutionProducer;

    /** Marks the worker unhealthy and reassigns (or fails) each execution it was running. */
    @Transactional
    public void handleWorkerOffline(UUID workerId) {
        workerRegistry.markUnhealthy(workerId);

        List<Execution> stranded =
                executionRepository.findByAssignedWorkerIdAndStatus(workerId, ExecutionStatus.RUNNING);

        if (stranded.isEmpty()) {
            log.info("Worker {} went offline with no running executions to reassign", workerId);
            return;
        }

        log.warn("Worker {} went offline; reassigning {} running execution(s)", workerId, stranded.size());
        for (Execution execution : stranded) {
            Optional<UUID> replacement = workerAssignmentService.selectWorker(Set.of(workerId));
            if (replacement.isPresent()) {
                reassign(execution, replacement.get());
            } else {
                fail(execution, workerId);
            }
        }
    }

    private void reassign(Execution execution, UUID newWorkerId) {
        execution.setAssignedWorkerId(newWorkerId);
        executionRepository.save(execution);

        Test test = execution.getTest();
        TestExecutionJob job = new TestExecutionJob(
                execution.getId(),
                test.getId(),
                test.getTargetUrl(),
                test.getHttpMethod(),
                test.getVirtualUsers(),
                test.getDurationSeconds(),
                newWorkerId,
                Instant.now());
        testExecutionProducer.publishJob(job);
        log.info("Reassigned execution {} to worker {}", execution.getId(), newWorkerId);
    }

    private void fail(Execution execution, UUID deadWorkerId) {
        execution.setStatus(ExecutionStatus.FAILED);
        execution.setFinishedAt(Instant.now());
        execution.setErrorMessage(
                "Worker %s went offline and no healthy worker was available for reassignment"
                        .formatted(deadWorkerId));
        executionRepository.save(execution);
        log.error("Failed execution {}: worker {} offline and no healthy worker to reassign to",
                execution.getId(), deadWorkerId);
    }
}
