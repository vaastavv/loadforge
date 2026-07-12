package com.loadforge.testservice.service;

import com.loadforge.testservice.domain.Execution;
import com.loadforge.testservice.domain.ExecutionStatus;
import com.loadforge.testservice.repository.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Completes executions whose configured duration has elapsed. A worker runs each test for its
 * {@code durationSeconds} and then goes idle; this sweep closes the execution record shortly
 * afterwards so a finished run stops showing as RUNNING once the worker is no longer generating load.
 *
 * <p>A short grace period is applied after the nominal end so the worker's final metrics snapshot is
 * ingested before the execution is marked terminal. Executions that were manually STOPPED or FAILED
 * are left untouched.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionCompletionService {

    private static final long GRACE_SECONDS = 3;

    private final ExecutionRepository executionRepository;

    @Scheduled(fixedDelayString = "${execution.completion.sweep-interval-ms:5000}")
    @Transactional
    public void completeElapsedExecutions() {
        List<Execution> running = executionRepository.findByStatus(ExecutionStatus.RUNNING);
        if (running.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        List<Execution> completed = new ArrayList<>();
        for (Execution execution : running) {
            Integer durationSeconds = execution.getTest() != null
                    ? execution.getTest().getDurationSeconds() : null;
            if (execution.getStartedAt() == null || durationSeconds == null) {
                continue;
            }
            Instant expectedEnd = execution.getStartedAt().plusSeconds(durationSeconds + GRACE_SECONDS);
            if (now.isAfter(expectedEnd)) {
                execution.setStatus(ExecutionStatus.COMPLETED);
                execution.setFinishedAt(now);
                completed.add(execution);
            }
        }

        if (!completed.isEmpty()) {
            executionRepository.saveAll(completed);
            log.info("Marked {} execution(s) COMPLETED after their configured duration elapsed", completed.size());
        }
    }
}
