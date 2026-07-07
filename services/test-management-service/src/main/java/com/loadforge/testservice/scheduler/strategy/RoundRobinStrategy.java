package com.loadforge.testservice.scheduler.strategy;

import com.loadforge.testservice.scheduler.model.WorkerNode;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Simple round-robin distribution that splits virtual users as evenly as possible across all
 * workers, ignoring their existing load. Included to demonstrate the pluggable strategy design.
 */
@Component
public class RoundRobinStrategy implements LoadDistributionStrategy {

    public static final String NAME = "round-robin";

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

        List<WorkerNode> ordered = workers.stream()
                .sorted(Comparator.comparing(WorkerNode::getWorkerId))
                .toList();
        int base = totalVirtualUsers / ordered.size();
        int remainder = totalVirtualUsers % ordered.size();
        for (int i = 0; i < ordered.size(); i++) {
            assigned.put(ordered.get(i).getWorkerId(), base + (i < remainder ? 1 : 0));
        }
        return assigned;
    }
}
