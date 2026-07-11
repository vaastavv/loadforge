package com.loadforge.workerservice.repository;

import com.loadforge.workerservice.domain.Worker;
import com.loadforge.workerservice.domain.WorkerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WorkerRepository extends JpaRepository<Worker, UUID> {

    List<Worker> findByStatusNotAndLastHeartbeatBefore(WorkerStatus status, Instant cutoff);

    List<Worker> findByStatusNot(WorkerStatus status);

    long countByStatus(WorkerStatus status);
}
