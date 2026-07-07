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
        workers.compute(workerId, (id, existing) ->
                existing != null ? markHealthy(existing) : new WorkerNode(id, true, 0));
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
