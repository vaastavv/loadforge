package com.loadforge.testservice.scheduler.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Mutable in-memory view of a worker used by the scheduler: its reported hostname, its health,
 * and its current assigned load (number of virtual users).
 */
@Getter
public class WorkerNode {

    private final UUID workerId;

    @Setter
    private String hostname;

    @Setter
    private boolean healthy;

    @Setter
    private int currentLoad;

    /** Worker-reported lifecycle status (ACTIVE/BUSY); OFFLINE is derived from {@link #healthy}. */
    @Setter
    private String status = "ACTIVE";

    /** Creates a node whose hostname defaults to the worker id until a heartbeat reports one. */
    public WorkerNode(UUID workerId, boolean healthy, int currentLoad) {
        this(workerId, workerId.toString(), healthy, currentLoad);
    }

    public WorkerNode(UUID workerId, String hostname, boolean healthy, int currentLoad) {
        this.workerId = workerId;
        this.hostname = hostname;
        this.healthy = healthy;
        this.currentLoad = currentLoad;
    }

    /** Returns an independent copy so registry snapshots cannot be mutated by callers. */
    public WorkerNode copy() {
        WorkerNode copy = new WorkerNode(workerId, hostname, healthy, currentLoad);
        copy.setStatus(status);
        return copy;
    }
}
