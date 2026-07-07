package com.loadforge.testservice.scheduler.strategy;

import com.loadforge.testservice.scheduler.model.WorkerNode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Strategy for distributing a number of virtual users across a set of healthy workers.
 *
 * <p>Future extensibility: add a new algorithm by creating another Spring {@code @Component}
 * that implements this interface and returns a unique {@link #name()}. It becomes selectable
 * immediately with no changes to the scheduler.
 */
public interface LoadDistributionStrategy {

    /** Unique, stable identifier used to select this strategy. */
    String name();

    /**
     * Distributes {@code totalVirtualUsers} across the given workers.
     *
     * @param totalVirtualUsers number of virtual users to assign (never negative)
     * @param workers           candidate workers (assumed healthy); each exposes its existing load
     * @return map of worker id to the number of <em>new</em> virtual users assigned to it
     */
    Map<UUID, Integer> distribute(int totalVirtualUsers, List<WorkerNode> workers);
}
