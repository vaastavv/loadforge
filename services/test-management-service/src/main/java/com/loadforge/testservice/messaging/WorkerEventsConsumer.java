package com.loadforge.testservice.messaging;

import com.loadforge.testservice.dto.MetricsReport;
import com.loadforge.testservice.dto.WorkerHeartbeat;
import com.loadforge.testservice.metrics.MetricsService;
import com.loadforge.testservice.scheduler.WorkerRegistry;
import com.loadforge.testservice.service.WorkerFailoverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes worker events: liveness from {@code worker-heartbeat} and results from {@code metrics}.
 * Metrics payloads are persisted for aggregation via {@link MetricsService}. Liveness events keep
 * the {@link WorkerRegistry} current and drive failover of executions when a worker goes offline.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerEventsConsumer {

    private final MetricsService metricsService;
    private final WorkerRegistry workerRegistry;
    private final WorkerFailoverService workerFailoverService;

    @KafkaListener(topics = KafkaConfig.WORKER_HEARTBEAT_TOPIC)
    public void onWorkerHeartbeat(WorkerHeartbeat heartbeat) {
        log.info("Received heartbeat from worker {} with status {} at {}",
                heartbeat.workerId(), heartbeat.status(), heartbeat.timestamp());
        if ("OFFLINE".equalsIgnoreCase(heartbeat.status())) {
            workerFailoverService.handleWorkerOffline(heartbeat.workerId());
        } else {
            workerRegistry.register(heartbeat.workerId());
        }
    }

    @KafkaListener(topics = KafkaConfig.METRICS_TOPIC)
    public void onMetrics(MetricsReport report) {
        log.info("Received metrics for execution {} from worker {}: total={}, success={}, failed={}, avgLatencyMs={}",
                report.executionId(), report.workerId(), report.totalRequests(),
                report.successfulRequests(), report.failedRequests(), report.averageLatencyMs());
        metricsService.ingest(report);
    }
}
