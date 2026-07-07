package com.loadforge.testservice.scheduler;

import com.loadforge.testservice.scheduler.model.SchedulePlan;
import com.loadforge.testservice.scheduler.model.WorkerAssignment;
import com.loadforge.testservice.scheduler.strategy.LeastLoadedStrategy;
import com.loadforge.testservice.scheduler.strategy.LoadDistributionStrategy;
import com.loadforge.testservice.scheduler.strategy.RoundRobinStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoadDistributionSchedulerTest {

    private WorkerRegistry registry;
    private LoadDistributionScheduler scheduler;

    private final UUID w1 = uuid(1);
    private final UUID w2 = uuid(2);
    private final UUID w3 = uuid(3);
    private final UUID w4 = uuid(4);

    @BeforeEach
    void setUp() {
        registry = new WorkerRegistry();
        List<LoadDistributionStrategy> strategies = List.of(new LeastLoadedStrategy(), new RoundRobinStrategy());
        scheduler = new LoadDistributionScheduler(registry, strategies, LeastLoadedStrategy.NAME);
    }

    @Test
    void schedulesEvenlyAcrossFourHealthyWorkers() {
        registerHealthy(w1, w2, w3, w4);

        SchedulePlan plan = scheduler.schedule(10_000);

        assertThat(plan.assignments()).hasSize(4);
        assertThat(plan.assignments()).extracting(WorkerAssignment::virtualUsers).containsOnly(2_500);
        assertThat(plan.assignedVirtualUsers()).isEqualTo(10_000);
        assertThat(plan.unassignedVirtualUsers()).isZero();
        assertThat(plan.strategy()).isEqualTo(LeastLoadedStrategy.NAME);
    }

    @Test
    void ignoresUnhealthyWorkers() {
        registerHealthy(w1, w2, w3);
        registry.register(w4);
        registry.markUnhealthy(w4);

        SchedulePlan plan = scheduler.schedule(9_000);

        assertThat(plan.assignments()).hasSize(3);
        assertThat(plan.assignments()).extracting(WorkerAssignment::workerId).doesNotContain(w4);
        assertThat(plan.assignedVirtualUsers()).isEqualTo(9_000);
    }

    @Test
    void returnsUnassignedPlanWhenNoHealthyWorkers() {
        SchedulePlan plan = scheduler.schedule(5_000);

        assertThat(plan.assignments()).isEmpty();
        assertThat(plan.assignedVirtualUsers()).isZero();
        assertThat(plan.unassignedVirtualUsers()).isEqualTo(5_000);
    }

    @Test
    void selectsNamedStrategy() {
        registerHealthy(w1, w2, w3, w4);

        SchedulePlan plan = scheduler.schedule(10_000, RoundRobinStrategy.NAME);

        assertThat(plan.strategy()).isEqualTo(RoundRobinStrategy.NAME);
        assertThat(plan.assignments()).extracting(WorkerAssignment::virtualUsers).containsOnly(2_500);
    }

    @Test
    void rejectsUnknownStrategy() {
        registerHealthy(w1);

        assertThatThrownBy(() -> scheduler.schedule(100, "does-not-exist"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeVirtualUsers() {
        assertThatThrownBy(() -> scheduler.schedule(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsOverByRedistributingFailedWorkerLoad() {
        registerHealthy(w1, w2, w3, w4);
        SchedulePlan original = scheduler.schedule(10_000);

        registry.markUnhealthy(w4);
        SchedulePlan reassigned = scheduler.reassign(original, w4);

        assertThat(reassigned.assignments()).hasSize(3);
        assertThat(reassigned.assignments()).extracting(WorkerAssignment::workerId).doesNotContain(w4);
        assertThat(reassigned.assignedVirtualUsers()).isEqualTo(10_000);
        assertThat(reassigned.unassignedVirtualUsers()).isZero();
        // 10,000 rebalanced across the 3 survivors: balanced within one VU.
        assertThat(reassigned.assignments()).extracting(WorkerAssignment::virtualUsers)
                .allSatisfy(v -> assertThat(v).isBetween(3_333, 3_334));
    }

    @Test
    void reassignFillsIdleSpareWorkerFirst() {
        // Original plan ran on w1 and w2 only; w3 is a healthy spare with no load.
        registerHealthy(w1, w2, w3);
        SchedulePlan original = SchedulePlan.of(200, LeastLoadedStrategy.NAME,
                List.of(new WorkerAssignment(w1, 100), new WorkerAssignment(w2, 100)));

        registry.markUnhealthy(w1);
        SchedulePlan reassigned = scheduler.reassign(original, w1);

        assertThat(reassigned.assignedVirtualUsers()).isEqualTo(200);
        assertThat(reassigned.unassignedVirtualUsers()).isZero();
        assertThat(reassigned.assignments()).extracting(WorkerAssignment::workerId).doesNotContain(w1);
        // w1's 100 orphaned VUs go to the idle spare w3 first (least loaded), leaving w2 at 100.
        assertThat(reassigned.assignments()).contains(new WorkerAssignment(w2, 100), new WorkerAssignment(w3, 100));
    }

    @Test
    void reassignReturnsUnassignedWhenNoSurvivors() {
        registerHealthy(w1);
        SchedulePlan original = scheduler.schedule(1_000);

        registry.markUnhealthy(w1);
        SchedulePlan reassigned = scheduler.reassign(original, w1);

        assertThat(reassigned.assignments()).isEmpty();
        assertThat(reassigned.unassignedVirtualUsers()).isEqualTo(1_000);
    }

    private void registerHealthy(UUID... ids) {
        for (UUID id : ids) {
            registry.register(id);
        }
    }

    private static UUID uuid(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }
}
