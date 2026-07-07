package com.loadforge.testservice.scheduler;

import com.loadforge.testservice.scheduler.model.SchedulePlan;
import com.loadforge.testservice.scheduler.model.WorkerAssignment;
import com.loadforge.testservice.scheduler.model.WorkerNode;
import com.loadforge.testservice.scheduler.strategy.LeastLoadedStrategy;
import com.loadforge.testservice.scheduler.strategy.LoadDistributionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Health-aware load distribution scheduler.
 *
 * <p>Splits a test's virtual users across the currently healthy workers using a pluggable
 * {@link LoadDistributionStrategy}, and supports failover by redistributing a failed worker's
 * virtual users onto the remaining healthy workers (least-loaded rebalancing).
 *
 * <p>Scheduling is a pure function of the registry snapshot and the requested load; it does not
 * mutate worker state, which keeps it deterministic and easy to reason about.
 */
@Slf4j
@Service
public class LoadDistributionScheduler {

    private final WorkerRegistry workerRegistry;
    private final Map<String, LoadDistributionStrategy> strategiesByName;
    private final String defaultStrategyName;

    public LoadDistributionScheduler(
            WorkerRegistry workerRegistry,
            List<LoadDistributionStrategy> strategies,
            @Value("${scheduler.default-strategy:least-loaded}") String defaultStrategyName) {
        this.workerRegistry = workerRegistry;
        this.strategiesByName = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(LoadDistributionStrategy::name, Function.identity()));
        this.defaultStrategyName = defaultStrategyName;
    }

    /** Schedules using the configured default strategy. */
    public SchedulePlan schedule(int totalVirtualUsers) {
        return schedule(totalVirtualUsers, defaultStrategyName);
    }

    /** Schedules using the named strategy. */
    public SchedulePlan schedule(int totalVirtualUsers, String strategyName) {
        if (totalVirtualUsers < 0) {
            throw new IllegalArgumentException("totalVirtualUsers must be >= 0");
        }
        LoadDistributionStrategy strategy = resolve(strategyName);
        List<WorkerNode> healthyWorkers = workerRegistry.healthyWorkers();

        if (healthyWorkers.isEmpty()) {
            log.warn("No healthy workers available to schedule {} virtual users", totalVirtualUsers);
            return SchedulePlan.of(totalVirtualUsers, strategy.name(), List.of());
        }

        Map<UUID, Integer> distribution = strategy.distribute(totalVirtualUsers, healthyWorkers);
        SchedulePlan plan = SchedulePlan.of(totalVirtualUsers, strategy.name(), toAssignments(distribution));
        log.info("Scheduled {} virtual users across {} healthy worker(s) using '{}' strategy",
                totalVirtualUsers, healthyWorkers.size(), strategy.name());
        return plan;
    }

    /**
     * Redistributes the virtual users of a failed worker across the remaining healthy workers,
     * preserving the assignments of the survivors. Any surviving worker that is no longer healthy
     * has its load treated as orphaned and redistributed as well.
     */
    public SchedulePlan reassign(SchedulePlan currentPlan, UUID failedWorkerId) {
        Set<UUID> healthyIds = workerRegistry.healthyWorkers().stream()
                .map(WorkerNode::getWorkerId)
                .collect(Collectors.toSet());

        int orphanedVirtualUsers = 0;
        Map<UUID, Integer> baseLoad = new LinkedHashMap<>();
        for (WorkerAssignment assignment : currentPlan.assignments()) {
            boolean lost = assignment.workerId().equals(failedWorkerId)
                    || !healthyIds.contains(assignment.workerId());
            if (lost) {
                orphanedVirtualUsers += assignment.virtualUsers();
            } else {
                baseLoad.merge(assignment.workerId(), assignment.virtualUsers(), Integer::sum);
            }
        }

        // Include healthy workers that were not part of the original plan (spare capacity).
        for (UUID healthyId : healthyIds) {
            if (!healthyId.equals(failedWorkerId)) {
                baseLoad.putIfAbsent(healthyId, 0);
            }
        }

        if (baseLoad.isEmpty()) {
            log.warn("No healthy workers available to absorb {} orphaned virtual users from worker {}",
                    orphanedVirtualUsers, failedWorkerId);
            return SchedulePlan.of(currentPlan.totalVirtualUsers(), LeastLoadedStrategy.NAME, List.of());
        }

        List<WorkerNode> survivors = baseLoad.entrySet().stream()
                .map(entry -> new WorkerNode(entry.getKey(), true, entry.getValue()))
                .toList();

        LoadDistributionStrategy leastLoaded = resolve(LeastLoadedStrategy.NAME);
        Map<UUID, Integer> extra = leastLoaded.distribute(orphanedVirtualUsers, survivors);

        List<WorkerAssignment> finalAssignments = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : baseLoad.entrySet()) {
            int vus = entry.getValue() + extra.getOrDefault(entry.getKey(), 0);
            if (vus > 0) {
                finalAssignments.add(new WorkerAssignment(entry.getKey(), vus));
            }
        }

        log.info("Reassigned {} orphaned virtual users from worker {} across {} healthy worker(s)",
                orphanedVirtualUsers, failedWorkerId, survivors.size());
        return SchedulePlan.of(currentPlan.totalVirtualUsers(), LeastLoadedStrategy.NAME, finalAssignments);
    }

    private List<WorkerAssignment> toAssignments(Map<UUID, Integer> distribution) {
        return distribution.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> new WorkerAssignment(entry.getKey(), entry.getValue()))
                .toList();
    }

    private LoadDistributionStrategy resolve(String strategyName) {
        LoadDistributionStrategy strategy = strategiesByName.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown load distribution strategy: " + strategyName);
        }
        return strategy;
    }
}
