package com.loadforge.workerservice.service;

import com.loadforge.workerservice.domain.WorkerStatus;
import com.loadforge.workerservice.dto.MetricsReport;
import com.loadforge.workerservice.dto.TestExecutionJob;
import com.loadforge.workerservice.messaging.MetricsPublisher;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;

/**
 * Runs a load test on this worker: it drives {@code virtualUsers} closed-loop HTTP clients against
 * the target for {@code durationSeconds}, streams cumulative metrics to Kafka, and holds the worker
 * in {@link WorkerStatus#BUSY} for the duration of the run.
 *
 * <p>Each job runs on a dedicated pool off the Kafka listener thread so message consumption and
 * heartbeats are never blocked while a run is in progress. Load is closed-loop (each virtual user
 * waits for its response before issuing the next request), so concurrency is bounded by
 * {@code virtualUsers} and naturally self-throttles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoadGeneratorService {

    private static final Duration REPORT_INTERVAL = Duration.ofSeconds(2);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);

    private final HeartbeatService heartbeatService;
    private final MetricsPublisher metricsPublisher;

    private final ExecutorService jobExecutor = Executors.newCachedThreadPool();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** Submits a job to run asynchronously and returns immediately. */
    public void submit(TestExecutionJob job) {
        jobExecutor.submit(() -> runJob(job));
    }

    private void runJob(TestExecutionJob job) {
        log.info("Starting load run for execution {} ({} VUs, {}s) against {}",
                job.executionId(), job.virtualUsers(), job.durationSeconds(), job.targetUrl());

        setStatus(job, WorkerStatus.BUSY);

        LongAdder total = new LongAdder();
        LongAdder success = new LongAdder();
        LongAdder failed = new LongAdder();
        LongAdder latencySumMs = new LongAdder();

        Instant deadline = Instant.now().plusSeconds(Math.max(1, job.durationSeconds()));
        int vus = Math.max(1, job.virtualUsers());
        ExecutorService vuPool = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<?>> vuTasks = new ArrayList<>();
        try {
            for (int i = 0; i < vus; i++) {
                vuTasks.add(vuPool.submit(() ->
                        driveUser(job, deadline, total, success, failed, latencySumMs)));
            }

            // Stream cumulative metrics and re-assert BUSY until the run finishes.
            while (Instant.now().isBefore(deadline)) {
                sleep(REPORT_INTERVAL.toMillis());
                publishMetrics(job, total, success, failed, latencySumMs);
                setStatus(job, WorkerStatus.BUSY);
            }

            for (Future<?> task : vuTasks) {
                awaitQuietly(task);
            }
        } finally {
            vuPool.shutdownNow();
            publishMetrics(job, total, success, failed, latencySumMs); // final snapshot
            setStatus(job, WorkerStatus.ACTIVE);
            log.info("Finished load run for execution {}: total={}, success={}, failed={}",
                    job.executionId(), total.sum(), success.sum(), failed.sum());
        }
    }

    private void driveUser(TestExecutionJob job, Instant deadline,
                           LongAdder total, LongAdder success, LongAdder failed, LongAdder latencySumMs) {
        HttpRequest request;
        try {
            String method = (job.httpMethod() == null || job.httpMethod().isBlank())
                    ? "GET" : job.httpMethod().toUpperCase();
            request = HttpRequest.newBuilder()
                    .uri(URI.create(job.targetUrl()))
                    .timeout(REQUEST_TIMEOUT)
                    .method(method, HttpRequest.BodyPublishers.noBody())
                    .build();
        } catch (RuntimeException e) {
            log.warn("Invalid request for execution {}: {}", job.executionId(), e.getMessage());
            return;
        }

        while (Instant.now().isBefore(deadline) && !Thread.currentThread().isInterrupted()) {
            long startNanos = System.nanoTime();
            try {
                HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
                total.increment();
                latencySumMs.add(elapsedMs);
                if (response.statusCode() >= 200 && response.statusCode() < 400) {
                    success.increment();
                } else {
                    failed.increment();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                total.increment();
                failed.increment();
            }
        }
    }

    private void publishMetrics(TestExecutionJob job,
                                LongAdder total, LongAdder success, LongAdder failed, LongAdder latencySumMs) {
        long totalRequests = total.sum();
        double avgLatency = totalRequests > 0 ? (double) latencySumMs.sum() / totalRequests : 0.0;
        metricsPublisher.publish(new MetricsReport(
                job.executionId(),
                job.assignedWorkerId(),
                totalRequests,
                success.sum(),
                failed.sum(),
                avgLatency,
                Instant.now()));
    }

    private void setStatus(TestExecutionJob job, WorkerStatus status) {
        if (job.assignedWorkerId() != null) {
            heartbeatService.updateStatus(job.assignedWorkerId(), status);
        }
    }

    private static void awaitQuietly(Future<?> task) {
        try {
            task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.debug("Virtual user task ended with {}", e.toString());
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    void shutdown() {
        jobExecutor.shutdownNow();
    }
}
