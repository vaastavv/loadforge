package com.loadforge.testservice.mapper;

import com.loadforge.testservice.domain.Execution;
import com.loadforge.testservice.dto.ExecutionResponse;

public final class ExecutionMapper {

    private ExecutionMapper() {
    }

    public static ExecutionResponse toResponse(Execution execution) {
        return new ExecutionResponse(
                execution.getId(),
                execution.getTest().getId(),
                execution.getStatus(),
                execution.getStartedAt(),
                execution.getFinishedAt(),
                execution.getErrorMessage(),
                execution.getCreatedAt()
        );
    }
}
