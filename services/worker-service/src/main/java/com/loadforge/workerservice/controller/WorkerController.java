package com.loadforge.workerservice.controller;

import com.loadforge.workerservice.dto.HeartbeatRequest;
import com.loadforge.workerservice.dto.RegisterWorkerRequest;
import com.loadforge.workerservice.dto.WorkerResponse;
import com.loadforge.workerservice.service.HeartbeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WorkerController {

    private final HeartbeatService heartbeatService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @PostMapping("/register")
    public ResponseEntity<WorkerResponse> register(@Valid @RequestBody RegisterWorkerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(heartbeatService.register(request));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<WorkerResponse> heartbeat(@Valid @RequestBody HeartbeatRequest request) {
        return ResponseEntity.ok(heartbeatService.heartbeat(request));
    }
}
