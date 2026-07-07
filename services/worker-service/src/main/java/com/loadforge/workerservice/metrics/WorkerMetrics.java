package com.loadforge.workerservice.metrics;

import com.loadforge.workerservice.domain.WorkerStatus;
import com.loadforge.workerservice.repository.WorkerRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

/**
 * Custom Prometheus/Micrometer metrics for the worker service.
 *
 * <p>CPU and memory are already captured automatically by the Actuator-provided JVM/system
 * binders ({@code process_cpu_usage}, {@code system_cpu_usage}, {@code jvm_memory_used_bytes},
 * {@code jvm_memory_max_bytes}), so the only bespoke instrumentation required here is the
 * domain gauge below. A worker in {@link WorkerStatus#BUSY} is one actively running an
 * execution, so the count of BUSY workers is exposed as the active-executions gauge.</p>
 *
 * <p>Registered as a {@link MeterBinder}, which Spring Boot binds to the primary
 * {@link MeterRegistry} on startup.</p>
 */
@Component
public class WorkerMetrics implements MeterBinder {

    private final WorkerRepository workerRepository;

    public WorkerMetrics(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("loadforge.worker.active.executions",
                        workerRepository,
                        repo -> (double) repo.countByStatus(WorkerStatus.BUSY))
                .description("Executions currently running on workers (workers in BUSY state)")
                .register(registry);
    }
}
