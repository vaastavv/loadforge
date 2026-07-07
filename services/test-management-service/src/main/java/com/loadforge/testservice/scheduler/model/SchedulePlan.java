package com.loadforge.testservice.scheduler.model;

import java.time.Instant;
import java.util.List;

/**
 * Result of a scheduling decision: how many virtual users each worker should run, plus any
 * virtual users that could not be assigned (e.g. no healthy workers).
 */
public record SchedulePlan(
        int totalVirtualUsers,
        int assignedVirtualUsers,
        int unassignedVirtualUsers,
        String strategy,
        List<WorkerAssignment> assignments,
        Instant createdAt
) {

    public static SchedulePlan of(int totalVirtualUsers, String strategy, List<WorkerAssignment> assignments) {
        int assigned = assignments.stream().mapToInt(WorkerAssignment::virtualUsers).sum();
        return new SchedulePlan(
                totalVirtualUsers,
                assigned,
                totalVirtualUsers - assigned,
                strategy,
                List.copyOf(assignments),
                Instant.now());
    }
}
