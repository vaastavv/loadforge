package com.loadforge.testservice.metrics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetricsController.class)
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetricsService metricsService;

    @Test
    void getExecutionMetrics_returns200WithSummary() throws Exception {
        UUID executionId = UUID.randomUUID();
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-01-01T00:00:10Z");
        ExecutionMetricsSummary summary = new ExecutionMetricsSummary(
                executionId, 10_000, 9_000, 1_000, 1_000.0, 900.0, 50.0, 95.0, 99.0, 4, start, end, 10.0);
        when(metricsService.getExecutionSummary(executionId)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/metrics/executions/{executionId}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(executionId.toString()))
                .andExpect(jsonPath("$.totalRequests").value(10000))
                .andExpect(jsonPath("$.successfulRequests").value(9000))
                .andExpect(jsonPath("$.failures").value(1000))
                .andExpect(jsonPath("$.requestsPerSecond").value(1000.0))
                .andExpect(jsonPath("$.throughput").value(900.0))
                .andExpect(jsonPath("$.averageLatencyMs").value(50.0))
                .andExpect(jsonPath("$.p95LatencyMs").value(95.0))
                .andExpect(jsonPath("$.p99LatencyMs").value(99.0))
                .andExpect(jsonPath("$.sampleCount").value(4));
    }

    @Test
    void getExecutionMetrics_returnsZeroedSummaryWhenNoData() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(metricsService.getExecutionSummary(executionId))
                .thenReturn(ExecutionMetricsSummary.empty(executionId));

        mockMvc.perform(get("/api/v1/metrics/executions/{executionId}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sampleCount").value(0))
                .andExpect(jsonPath("$.requestsPerSecond").value(0.0))
                .andExpect(jsonPath("$.p99LatencyMs").value(0.0));
    }
}
