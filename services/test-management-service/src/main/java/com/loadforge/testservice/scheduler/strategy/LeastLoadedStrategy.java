package com.loadforge.testservice.scheduler.strategy;

import com.loadforge.testservice.scheduler.model.WorkerNode;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

/**
 * Greedy least-loaded distribution: each virtual user is placed on the worker with the smallest
 * projected load. Equal or idle workers therefore receive an even split
 * (e.g. 10,000 VUs across 4 idle workers -&gt; 2,500 each), while pre-loaded workers are balanced
 * so the busiest worker ends up as lightly loaded as possible.
 */
@Component
public class LeastLoadedStrategy implements LoadDistributionStrategy {

    public static final String NAME = "least-loaded";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Map<UUID, Integer> distribute(int totalVirtualUsers, List<WorkerNode> workers) {
        Map<UUID, Integer> assigned = new LinkedHashMap<>();
        for (WorkerNode worker : workers) {
            assigned.put(worker.getWorkerId(), 0);
        }
        if (workers.isEmpty() || totalVirtualUsers <= 0) {
            return assigned;
        }

        // Min-heap ordered by projected load; ties broken by worker id for deterministic output.
        PriorityQueue<Slot> heap = new PriorityQueue<>(
                Comparator.comparingLong((Slot slot) -> slot.projectedLoad).thenComparing(slot -> slot.workerId));
        for (WorkerNode worker : workers) {
            heap.add(new Slot(worker.getWorkerId(), worker.getCurrentLoad()));
        }

        for (int i = 0; i < totalVirtualUsers; i++) {
            Slot least = heap.poll();
            least.projectedLoad++;
            least.assigned++;
            heap.add(least);
        }

        for (Slot slot : heap) {
            assigned.put(slot.workerId, slot.assigned);
        }
        return assigned;
    }

    private static final class Slot {
        private final UUID workerId;
        private long projectedLoad;
        private int assigned;

        private Slot(UUID workerId, long initialLoad) {
            this.workerId = workerId;
            this.projectedLoad = initialLoad;
        }
    }
}
