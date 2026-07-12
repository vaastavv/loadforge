package com.loadforge.testservice.scheduler;

import com.loadforge.testservice.scheduler.model.WorkerNode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory registry of workers known to the control plane, tracking each worker's
 * health and current load. This is the health-awareness source consumed by the scheduler.
 *
 * <p>It is intentionally decoupled from any transport: heartbeats, metrics, or admin actions can
 * feed it via {@link #register}, {@link #markHealthy}, {@link #markUnhealthy} and {@link #updateLoad}.
 */
@Service
public class WorkerRegistry {

    private final ConcurrentHashMap<UUID, WorkerNode> workers = new ConcurrentHashMap<>();

    /** Registers a new worker (healthy, zero load) or marks an existing one healthy. */
    public void register(UUID workerId) {
        register(workerId, null);
    }

    /**
     * Registers a worker with its reported hostname (healthy, zero load), or marks an existing one
     * healthy while refreshing its hostname whenever a non-blank value is reported.
     */
    public void register(UUID workerId, String hostname) {
        workers.compute(workerId, (id, existing) -> {
            if (existing != null) {
                existing.setHealthy(true);
                if (hostname != null && !hostname.isBlank()) {
                    existing.setHostname(hostname);
                }
                return existing;
            }
            String resolved = (hostname != null && !hostname.isBlank()) ? hostname : id.toString();
            return new WorkerNode(id, resolved, true, 0);
        });
    }

    public void markHealthy(UUID workerId) {
        workers.computeIfPresent(workerId, (id, node) -> markHealthy(node));
    }

    public void markUnhealthy(UUID workerId) {
        workers.computeIfPresent(workerId, (id, node) -> {
            node.setHealthy(false);
            return node;
        });
    }

    public void updateLoad(UUID workerId, int currentLoad) {
        workers.computeIfPresent(workerId, (id, node) -> {
            node.setCurrentLoad(currentLoad);
            return node;
        });
    }

    /** Updates a worker's reported lifecycle status (e.g. BUSY while running a job); no-op if unknown. */
    public void updateStatus(UUID workerId, String status) {
        if (status == null || status.isBlank()) {
            return;
        }
        workers.computeIfPresent(workerId, (id, node) -> {
            node.setStatus(status);
            return node;
        });
    }

    public void remove(UUID workerId) {
        workers.remove(workerId);
    }

    public Optional<WorkerNode> find(UUID workerId) {
        return Optional.ofNullable(workers.get(workerId)).map(WorkerNode::copy);
    }

    /** Snapshot copies of all currently healthy workers. */
    public List<WorkerNode> healthyWorkers() {
        return workers.values().stream()
                .filter(WorkerNode::isHealthy)
                .map(WorkerNode::copy)
                .toList();
    }

    /** Snapshot copies of all known workers. */
    public List<WorkerNode> allWorkers() {
        return workers.values().stream().map(WorkerNode::copy).toList();
    }

    private static WorkerNode markHealthy(WorkerNode node) {
        node.setHealthy(true);
        return node;
    }
}
