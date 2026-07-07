package com.loadforge.workerservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String hostname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkerStatus status;

    @Column(name = "last_heartbeat", nullable = false)
    private Instant lastHeartbeat;

    @CreationTimestamp
    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;
}
