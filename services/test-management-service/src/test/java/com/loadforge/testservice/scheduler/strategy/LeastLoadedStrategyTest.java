package com.loadforge.testservice.scheduler.strategy;

import com.loadforge.testservice.scheduler.model.WorkerNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class LeastLoadedStrategyTest {

    private final LeastLoadedStrategy strategy = new LeastLoadedStrategy();

    @Test
    void distributesEvenlyAcrossIdenticalIdleWorkers() {
        Map<UUID, Integer> result = strategy.distribute(10_000, idleWorkers(4));

        assertThat(result.values()).containsOnly(2_500);
        assertThat(sum(result)).isEqualTo(10_000);
    }

    @Test
    void distributesRemainderToLeastLoadedWorkers() {
        Map<UUID, Integer> result = strategy.distribute(10_002, idleWorkers(4));

        assertThat(sum(result)).isEqualTo(10_002);
        assertThat(result.values()).allSatisfy(v -> assertThat(v).isBetween(2_500, 2_501));
        assertThat(result.values().stream().filter(v -> v == 2_501)).hasSize(2);
    }

    @Test
    void favoursLeastLoadedWorkersWhenExistingLoadIsUneven() {
        UUID busy = uuid(1);
        UUID idle = uuid(2);
        UUID half = uuid(3);
        List<WorkerNode> workers = List.of(
                new WorkerNode(busy, true, 100),
                new WorkerNode(idle, true, 0),
                new WorkerNode(half, true, 50));

        Map<UUID, Integer> result = strategy.distribute(150, workers);

        // Balances all workers to a final load of 100 each.
        assertThat(result).containsEntry(busy, 0).containsEntry(idle, 100).containsEntry(half, 50);
        assertThat(sum(result)).isEqualTo(150);
    }

    @Test
    void assignsAllVirtualUsersToSingleWorker() {
        UUID only = uuid(1);
        Map<UUID, Integer> result = strategy.distribute(777, List.of(new WorkerNode(only, true, 0)));
        assertThat(result).containsEntry(only, 777);
    }

    @Test
    void returnsEmptyWhenNoWorkers() {
        assertThat(strategy.distribute(1_000, List.of())).isEmpty();
    }

    @Test
    void assignsNothingWhenTotalIsZero() {
        assertThat(strategy.distribute(0, idleWorkers(3)).values()).containsOnly(0);
    }

    private static int sum(Map<UUID, Integer> result) {
        return result.values().stream().mapToInt(Integer::intValue).sum();
    }

    private static List<WorkerNode> idleWorkers(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> new WorkerNode(uuid(i), true, 0))
                .toList();
    }

    private static UUID uuid(int n) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n));
    }
}
