package com.loadforge.testservice.metrics;

import com.loadforge.testservice.domain.ExecutionStatus;
import com.loadforge.testservice.repository.ExecutionRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

/**
 * Custom Prometheus/Micrometer metrics for the controller (test-management) service.
 *
 * <p>API request count and response latency are already captured automatically by the
 * Actuator-provided {@code http_server_requests_seconds} timer (count + histogram), so the
 * only bespoke instrumentation required here is the domain gauge below.</p>
 *
 * <p>Registered as a {@link MeterBinder}, which Spring Boot binds to the primary
 * {@link MeterRegistry} on startup.</p>
 */
@Component
public class ControllerMetrics implements MeterBinder {

    private final ExecutionRepository executionRepository;

    public ControllerMetrics(ExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("loadforge.controller.active.tests",
                        executionRepository,
                        repo -> (double) repo.countByStatus(ExecutionStatus.RUNNING))
                .description("Load tests currently running (executions in RUNNING state)")
                .register(registry);
    }
}
