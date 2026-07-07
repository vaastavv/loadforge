package com.loadforge.testservice.mapper;

import com.loadforge.testservice.domain.Test;
import com.loadforge.testservice.dto.CreateTestRequest;
import com.loadforge.testservice.dto.TestResponse;

public final class TestMapper {

    private TestMapper() {
    }

    public static Test toEntity(CreateTestRequest request) {
        return Test.builder()
                .name(request.name())
                .description(request.description())
                .targetUrl(request.targetUrl())
                .httpMethod(request.httpMethod())
                .virtualUsers(request.virtualUsers())
                .durationSeconds(request.durationSeconds())
                .build();
    }

    public static TestResponse toResponse(Test test) {
        return new TestResponse(
                test.getId(),
                test.getName(),
                test.getDescription(),
                test.getTargetUrl(),
                test.getHttpMethod(),
                test.getVirtualUsers(),
                test.getDurationSeconds(),
                test.getCreatedAt(),
                test.getUpdatedAt()
        );
    }
}
