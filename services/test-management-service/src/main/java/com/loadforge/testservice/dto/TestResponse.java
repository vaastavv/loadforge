package com.loadforge.testservice.dto;

import com.loadforge.testservice.domain.HttpMethod;

import java.time.Instant;
import java.util.UUID;

public record TestResponse(
        UUID id,
        String name,
        String description,
        String targetUrl,
        HttpMethod httpMethod,
        Integer virtualUsers,
        Integer durationSeconds,
        Instant createdAt,
        Instant updatedAt
) {
}
