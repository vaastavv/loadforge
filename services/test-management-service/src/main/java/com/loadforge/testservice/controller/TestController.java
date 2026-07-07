package com.loadforge.testservice.controller;

import com.loadforge.testservice.dto.CreateTestRequest;
import com.loadforge.testservice.dto.ExecutionResponse;
import com.loadforge.testservice.dto.TestResponse;
import com.loadforge.testservice.dto.TestStatusResponse;
import com.loadforge.testservice.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping
    public ResponseEntity<TestResponse> createTest(@Valid @RequestBody CreateTestRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        TestResponse response = testService.createTest(request);
        URI location = uriBuilder.path("/api/v1/tests/{id}/status")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ExecutionResponse> startTest(@PathVariable UUID id) {
        return ResponseEntity.ok(testService.startTest(id));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<ExecutionResponse> stopTest(@PathVariable UUID id) {
        return ResponseEntity.ok(testService.stopTest(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<TestStatusResponse> getTestStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(testService.getTestStatus(id));
    }

    @GetMapping("/{id}/executions")
    public ResponseEntity<List<ExecutionResponse>> listExecutions(@PathVariable UUID id) {
        return ResponseEntity.ok(testService.listExecutions(id));
    }
}
