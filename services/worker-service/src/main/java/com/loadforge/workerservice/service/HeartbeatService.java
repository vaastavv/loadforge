package com.loadforge.workerservice.service;

import com.loadforge.workerservice.domain.Worker;
import com.loadforge.workerservice.domain.WorkerStatus;
import com.loadforge.workerservice.dto.HeartbeatRequest;
import com.loadforge.workerservice.dto.RegisterWorkerRequest;
import com.loadforge.workerservice.dto.WorkerResponse;
import com.loadforge.workerservice.messaging.WorkerEventPublisher;
import com.loadforge.workerservice.repository.WorkerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HeartbeatService {

    private final WorkerRepository workerRepository;
    private final WorkerEventPublisher eventPublisher;
    private final long timeoutSeconds;

    public HeartbeatService(WorkerRepository workerRepository,
                            WorkerEventPublisher eventPublisher,
                            @Value("${worker.heartbeat.timeout-seconds}") long timeoutSeconds) {
        this.workerRepository = workerRepository;
        this.eventPublisher = eventPublisher;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Transactional
    public WorkerResponse register(RegisterWorkerRequest request) {
        Worker worker = Worker.builder()
                .hostname(request.hostname())
                .status(WorkerStatus.ACTIVE)
                .lastHeartbeat(Instant.now())
                .build();

        Worker saved = workerRepository.save(worker);
        log.info("Registered worker {} ({})", saved.getId(), saved.getHostname());
        eventPublisher.publishStatus(saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    @Transactional
    public WorkerResponse heartbeat(HeartbeatRequest request) {
        Worker worker = workerRepository.findById(request.workerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Worker not found: " + request.workerId()));

        worker.setLastHeartbeat(Instant.now());
        worker.setStatus(request.status() != null ? request.status() : WorkerStatus.ACTIVE);

        Worker saved = workerRepository.save(worker);
        log.debug("Heartbeat from worker {} -> {}", saved.getId(), saved.getStatus());
        eventPublisher.publishStatus(saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    @Scheduled(fixedDelayString = "${worker.heartbeat.sweep-interval-ms:10000}")
    @Transactional
    public void markStaleWorkersOffline() {
        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(timeoutSeconds);
        List<Worker> workers = workerRepository.findAll();
        List<Worker> stale = new ArrayList<>();
        List<Worker> refreshed = new ArrayList<>();

        for (Worker worker : workers) {
            if (worker.getStatus() == WorkerStatus.OFFLINE) {
                continue;
            }
            if (worker.getLastHeartbeat() == null || worker.getLastHeartbeat().isBefore(cutoff)) {
                worker.setStatus(WorkerStatus.OFFLINE);
                stale.add(worker);
            } else {
                worker.setLastHeartbeat(now);
                refreshed.add(worker);
            }
        }

        if (!refreshed.isEmpty()) {
            workerRepository.saveAll(refreshed);
            refreshed.forEach(worker -> eventPublisher.publishStatus(worker.getId(), worker.getStatus()));
        }

        if (!stale.isEmpty()) {
            workerRepository.saveAll(stale);
            log.info("Marked {} worker(s) OFFLINE after {}s heartbeat timeout", stale.size(), timeoutSeconds);
            stale.forEach(worker -> eventPublisher.publishStatus(worker.getId(), WorkerStatus.OFFLINE));
        }
    }

    private WorkerResponse toResponse(Worker worker) {
        return new WorkerResponse(
                worker.getId(),
                worker.getHostname(),
                worker.getStatus(),
                worker.getLastHeartbeat(),
                worker.getRegisteredAt());
    }
}
