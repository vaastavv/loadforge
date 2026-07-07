package com.loadforge.testservice.sla;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SlaController.class)
class SlaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SlaValidationService slaValidationService;

    @Test
    void validate_returnsOkWithVerdict() throws Exception {
        UUID executionId = UUID.randomUUID();
        SlaValidationResponse response = new SlaValidationResponse(
                executionId, SlaStatus.PASS, "default", Instant.now(),
                List.of(
                        new SlaRuleResultResponse("p95-latency", "p95LatencyMs", "<", 300.0, 250.0, SlaStatus.PASS),
                        new SlaRuleResultResponse("error-rate", "errorRatePercent", "<", 1.0, 0.5, SlaStatus.PASS)));
        when(slaValidationService.validateExecution(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/sla/executions/{executionId}/validate", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(executionId.toString()))
                .andExpect(jsonPath("$.status").value("PASS"))
                .andExpect(jsonPath("$.policyName").value("default"))
                .andExpect(jsonPath("$.rules[0].rule").value("p95-latency"))
                .andExpect(jsonPath("$.rules[0].status").value("PASS"));
    }

    @Test
    void getLatestResult_whenPresent_returnsOk() throws Exception {
        UUID executionId = UUID.randomUUID();
        SlaValidationResponse response = new SlaValidationResponse(
                executionId, SlaStatus.FAIL, "default", Instant.now(),
                List.of(new SlaRuleResultResponse("p95-latency", "p95LatencyMs", "<", 300.0, 420.0, SlaStatus.FAIL)));
        when(slaValidationService.getLatestResult(any())).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/sla/executions/{executionId}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.rules[0].status").value("FAIL"));
    }

    @Test
    void getLatestResult_whenAbsent_returnsNotFound() throws Exception {
        when(slaValidationService.getLatestResult(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/sla/executions/{executionId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getActivePolicy_returnsOk() throws Exception {
        SlaPolicyResponse response = new SlaPolicyResponse(
                UUID.randomUUID(), "default", 300.0, 1.0, true);
        when(slaValidationService.getActivePolicy()).thenReturn(response);

        mockMvc.perform(get("/api/v1/sla/policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("default"))
                .andExpect(jsonPath("$.maxP95LatencyMs").value(300.0))
                .andExpect(jsonPath("$.maxErrorRatePercent").value(1.0))
                .andExpect(jsonPath("$.active").value(true));
    }
}
