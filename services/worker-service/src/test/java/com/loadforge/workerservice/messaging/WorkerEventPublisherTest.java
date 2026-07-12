package com.loadforge.workerservice.messaging;

import com.loadforge.workerservice.domain.WorkerStatus;
import com.loadforge.workerservice.dto.WorkerStatusEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkerEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void publishStatus_sendsEventKeyedByWorkerId() {
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        WorkerEventPublisher publisher = new WorkerEventPublisher(kafkaTemplate, "worker-heartbeat");
        UUID workerId = UUID.randomUUID();

        publisher.publishStatus(workerId, "worker-01", WorkerStatus.OFFLINE);

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("worker-heartbeat"), eq(workerId.toString()), payload.capture());

        assertThat(payload.getValue()).isInstanceOf(WorkerStatusEvent.class);
        WorkerStatusEvent event = (WorkerStatusEvent) payload.getValue();
        assertThat(event.workerId()).isEqualTo(workerId);
        assertThat(event.hostname()).isEqualTo("worker-01");
        assertThat(event.status()).isEqualTo("OFFLINE");
        assertThat(event.timestamp()).isNotNull();
    }
}
