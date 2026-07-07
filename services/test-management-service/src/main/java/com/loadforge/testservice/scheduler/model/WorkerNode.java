package com.loadforge.testservice.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Mutable in-memory view of a worker used by the scheduler: its health and its current
 * assigned load (number of virtual users).
 */
@Getter
@AllArgsConstructor
public class WorkerNode {

    private final UUID workerId;

    @Setter
    private boolean healthy;

    @Setter
    private int currentLoad;

    /** Returns an independent copy so registry snapshots cannot be mutated by callers. */
    public WorkerNode copy() {
        return new WorkerNode(workerId, healthy, currentLoad);
    }
}
