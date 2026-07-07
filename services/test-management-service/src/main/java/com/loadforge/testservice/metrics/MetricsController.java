package com.loadforge.testservice.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/executions/{executionId}")
    public ResponseEntity<ExecutionMetricsSummary> getExecutionMetrics(@PathVariable UUID executionId) {
        return ResponseEntity.ok(metricsService.getExecutionSummary(executionId));
    }
}
