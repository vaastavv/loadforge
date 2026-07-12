package com.loadforge.workerservice.messaging;

import com.loadforge.workerservice.domain.WorkerStatus;
import com.loadforge.workerservice.dto.WorkerStatusEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Publishes worker liveness/status changes to Kafka so the control plane can react to them.
 *
 * <p>Sends are fire-and-forget: a broker outage must never fail a worker's REST registration or
 * heartbeat, so failures are logged from the async callback rather than propagated to the caller.
 */
@Slf4j
@Component
public class WorkerEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public WorkerEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                @Value("${worker.events.topic:worker-heartbeat}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /** Publishes the current status of a worker keyed by its id (preserves per-worker ordering). */
    public void publishStatus(UUID workerId, String hostname, WorkerStatus status) {
        WorkerStatusEvent event = new WorkerStatusEvent(workerId, hostname, status.name(), Instant.now());
        kafkaTemplate.send(topic, workerId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish {} status for worker {} to topic {}: {}",
                                status, workerId, topic, ex.getMessage());
                    } else {
                        log.debug("Published {} status for worker {} to topic {}", status, workerId, topic);
                    }
                });
    }
}
