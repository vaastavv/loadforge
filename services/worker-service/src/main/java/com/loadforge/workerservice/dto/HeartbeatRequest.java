package com.loadforge.workerservice.dto;

import com.loadforge.workerservice.domain.WorkerStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HeartbeatRequest(

        @NotNull
        UUID workerId,

        WorkerStatus status
) {
}
