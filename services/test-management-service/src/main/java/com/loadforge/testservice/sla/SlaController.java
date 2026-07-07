package com.loadforge.testservice.sla;

import com.loadforge.testservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sla")
@RequiredArgsConstructor
public class SlaController {

    private final SlaValidationService slaValidationService;

    @PostMapping("/executions/{executionId}/validate")
    public ResponseEntity<SlaValidationResponse> validate(@PathVariable UUID executionId) {
        return ResponseEntity.ok(slaValidationService.validateExecution(executionId));
    }

    @GetMapping("/executions/{executionId}")
    public ResponseEntity<SlaValidationResponse> getLatestResult(@PathVariable UUID executionId) {
        return slaValidationService.getLatestResult(executionId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No SLA validation result for execution: %s".formatted(executionId)));
    }

    @GetMapping("/policy")
    public ResponseEntity<SlaPolicyResponse> getActivePolicy() {
        return ResponseEntity.ok(slaValidationService.getActivePolicy());
    }
}
