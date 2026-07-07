package com.loadforge.testservice.scheduler.strategy;

import com.loadforge.testservice.scheduler.model.WorkerNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class RoundRobinStrategyTest {

    private final RoundRobinStrategy strategy = new RoundRobinStrategy();

    @Test
    void splitsEvenlyWhenDivisible() {
        assertThat(strategy.distribute(10_000, idleWorkers(4)).values()).containsOnly(2_500);
    }

    @Test
    void distributesRemainderToFirstWorkers() {
        Map<UUID, Integer> result = strategy.distribute(10, idleWorkers(3));

        assertThat(result.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(10);
        assertThat(result).containsEntry(uuid(1), 4).containsEntry(uuid(2), 3).containsEntry(uuid(3), 3);
    }

    @Test
    void ignoresExistingLoad() {
        Map<UUID, Integer> result = strategy.distribute(4, List.of(
                new WorkerNode(uuid(1), true, 999),
                new WorkerNode(uuid(2), true, 0)));

        assertThat(result).containsEntry(uuid(1), 2).containsEntry(uuid(2), 2);
    }

    @Test
    void returnsEmptyWhenNoWorkers() {
        assertThat(strategy.distribute(10, List.of())).isEmpty();
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
