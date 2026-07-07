package com.loadforge.testservice.scheduler.model;

import java.util.UUID;

/** Number of virtual users assigned to a single worker. */
public record WorkerAssignment(UUID workerId, int virtualUsers) {
}
