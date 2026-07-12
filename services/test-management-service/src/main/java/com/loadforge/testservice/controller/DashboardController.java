package com.loadforge.testservice.controller;

import com.loadforge.testservice.dto.ExecutionResponse;
import com.loadforge.testservice.dto.TestResponse;
import com.loadforge.testservice.dto.WorkerResponse;
import com.loadforge.testservice.scheduler.WorkerRegistry;
import com.loadforge.testservice.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DashboardController {

    private final TestService testService;
    private final WorkerRegistry workerRegistry;

    @GetMapping("/tests")
    public ResponseEntity<List<TestResponse>> listTests() {
        return ResponseEntity.ok(testService.listTests());
    }

    @GetMapping("/executions")
    public ResponseEntity<List<ExecutionResponse>> listAllExecutions() {
        return ResponseEntity.ok(testService.listAllExecutions());
    }

    @GetMapping("/workers")
    public ResponseEntity<List<WorkerResponse>> listWorkers() {
        List<WorkerResponse> workers = workerRegistry.allWorkers().stream()
                .map(node -> new WorkerResponse(
                        node.getWorkerId(),
                        node.getHostname(),
                        node.isHealthy() ? node.getStatus() : "OFFLINE",
                        Instant.now(),
                        Instant.now()))
                .toList();
        return ResponseEntity.ok(workers);
    }
}
