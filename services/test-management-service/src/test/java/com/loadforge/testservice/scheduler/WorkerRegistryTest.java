package com.loadforge.testservice.scheduler;

import com.loadforge.testservice.scheduler.model.WorkerNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkerRegistryTest {

    private WorkerRegistry registry;

    private final UUID worker1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID worker2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() {
        registry = new WorkerRegistry();
    }

    @Test
    void registerAddsHealthyWorkerWithZeroLoad() {
        registry.register(worker1);

        assertThat(registry.healthyWorkers()).singleElement().satisfies(worker -> {
            assertThat(worker.getWorkerId()).isEqualTo(worker1);
            assertThat(worker.isHealthy()).isTrue();
            assertThat(worker.getCurrentLoad()).isZero();
        });
    }

    @Test
    void markUnhealthyExcludesWorkerFromHealthyList() {
        registry.register(worker1);
        registry.register(worker2);

        registry.markUnhealthy(worker1);

        assertThat(registry.healthyWorkers()).extracting(WorkerNode::getWorkerId).containsExactly(worker2);
        assertThat(registry.allWorkers()).hasSize(2);
    }

    @Test
    void markHealthyRestoresWorker() {
        registry.register(worker1);
        registry.markUnhealthy(worker1);

        registry.markHealthy(worker1);

        assertThat(registry.healthyWorkers()).extracting(WorkerNode::getWorkerId).containsExactly(worker1);
    }

    @Test
    void updateLoadIsReflectedInSnapshot() {
        registry.register(worker1);

        registry.updateLoad(worker1, 1_234);

        assertThat(registry.find(worker1)).get()
                .satisfies(worker -> assertThat(worker.getCurrentLoad()).isEqualTo(1_234));
    }

    @Test
    void snapshotCopiesDoNotMutateRegistryState() {
        registry.register(worker1);

        WorkerNode snapshot = registry.healthyWorkers().get(0);
        snapshot.setCurrentLoad(9_999);
        snapshot.setHealthy(false);

        assertThat(registry.find(worker1)).get().satisfies(worker -> {
            assertThat(worker.getCurrentLoad()).isZero();
            assertThat(worker.isHealthy()).isTrue();
        });
    }

    @Test
    void removeDeletesWorker() {
        registry.register(worker1);

        registry.remove(worker1);

        assertThat(registry.allWorkers()).isEmpty();
    }
}
