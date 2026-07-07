package com.loadforge.testservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadforge.testservice.domain.ExecutionStatus;
import com.loadforge.testservice.domain.HttpMethod;
import com.loadforge.testservice.dto.CreateTestRequest;
import com.loadforge.testservice.dto.ExecutionResponse;
import com.loadforge.testservice.dto.TestResponse;
import com.loadforge.testservice.dto.TestStatusResponse;
import com.loadforge.testservice.exception.ResourceNotFoundException;
import com.loadforge.testservice.exception.TestStateConflictException;
import com.loadforge.testservice.service.TestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestService testService;

    @Test
    void createTest_withValidBody_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        CreateTestRequest request = new CreateTestRequest(
                "Checkout", "desc", "https://example.com", HttpMethod.GET, 100, 60);
        TestResponse response = new TestResponse(
                id, "Checkout", "desc", "https://example.com", HttpMethod.GET, 100, 60,
                Instant.now(), Instant.now());
        when(testService.createTest(any(CreateTestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Checkout"));
    }

    @Test
    void createTest_withBlankName_returns400() throws Exception {
        CreateTestRequest invalid = new CreateTestRequest(
                "  ", "desc", "https://example.com", HttpMethod.GET, 100, 60);

        mockMvc.perform(post("/api/v1/tests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void startTest_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        ExecutionResponse exec = new ExecutionResponse(
                UUID.randomUUID(), id, ExecutionStatus.RUNNING, Instant.now(), null, null, Instant.now());
        when(testService.startTest(eq(id))).thenReturn(exec);

        mockMvc.perform(post("/api/v1/tests/{id}/start", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.testId").value(id.toString()));
    }

    @Test
    void stopTest_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        ExecutionResponse exec = new ExecutionResponse(
                UUID.randomUUID(), id, ExecutionStatus.STOPPED, Instant.now(), Instant.now(), null, Instant.now());
        when(testService.stopTest(eq(id))).thenReturn(exec);

        mockMvc.perform(post("/api/v1/tests/{id}/stop", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STOPPED"));
    }

    @Test
    void getTestStatus_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        TestStatusResponse statusResponse = new TestStatusResponse(
                id, "Checkout", "RUNNING", UUID.randomUUID(), Instant.now());
        when(testService.getTestStatus(eq(id))).thenReturn(statusResponse);

        mockMvc.perform(get("/api/v1/tests/{id}/status", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.testName").value("Checkout"));
    }

    @Test
    void listExecutions_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        ExecutionResponse exec = new ExecutionResponse(
                UUID.randomUUID(), id, ExecutionStatus.COMPLETED, Instant.now(), Instant.now(), null, Instant.now());
        when(testService.listExecutions(eq(id))).thenReturn(List.of(exec));

        mockMvc.perform(get("/api/v1/tests/{id}/executions", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void getTestStatus_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(testService.getTestStatus(eq(id)))
                .thenThrow(new ResourceNotFoundException("Test not found: " + id));

        mockMvc.perform(get("/api/v1/tests/{id}/status", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void startTest_whenConflict_returns409() throws Exception {
        UUID id = UUID.randomUUID();
        when(testService.startTest(eq(id)))
                .thenThrow(new TestStateConflictException("already running"));

        mockMvc.perform(post("/api/v1/tests/{id}/start", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
