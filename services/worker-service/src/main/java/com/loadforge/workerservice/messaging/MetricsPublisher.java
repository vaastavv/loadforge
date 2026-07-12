package com.loadforge.workerservice.messaging;

import com.loadforge.workerservice.dto.MetricsReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes execution metrics to Kafka for the control plane to aggregate.
 *
 * <p>Sends are fire-and-forget: a broker hiccup must never abort an in-flight load run, so failures
 * are logged from the async callback rather than propagated to the caller.
 */
@Slf4j
@Component
public class MetricsPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public MetricsPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                            @Value("${worker.metrics.topic:metrics}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /** Publishes a cumulative metrics snapshot for an execution, keyed by execution id for ordering. */
    public void publish(MetricsReport report) {
        kafkaTemplate.send(topic, report.executionId().toString(), report)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish metrics for execution {} to topic {}: {}",
                                report.executionId(), topic, ex.getMessage());
                    } else {
                        log.debug("Published metrics for execution {} to topic {}", report.executionId(), topic);
                    }
                });
    }
}
