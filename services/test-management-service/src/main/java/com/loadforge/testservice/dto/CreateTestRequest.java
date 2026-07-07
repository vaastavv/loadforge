package com.loadforge.testservice.dto;

import com.loadforge.testservice.domain.HttpMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTestRequest(

        @NotBlank(message = "name is required")
        @Size(max = 255, message = "name must be at most 255 characters")
        String name,

        @Size(max = 1000, message = "description must be at most 1000 characters")
        String description,

        @NotBlank(message = "targetUrl is required")
        @Pattern(regexp = "^https?://.+", message = "targetUrl must be a valid http(s) URL")
        @Size(max = 2048, message = "targetUrl must be at most 2048 characters")
        String targetUrl,

        @NotNull(message = "httpMethod is required")
        HttpMethod httpMethod,

        @NotNull(message = "virtualUsers is required")
        @Min(value = 1, message = "virtualUsers must be at least 1")
        @Max(value = 100000, message = "virtualUsers must be at most 100000")
        Integer virtualUsers,

        @NotNull(message = "durationSeconds is required")
        @Min(value = 1, message = "durationSeconds must be at least 1")
        @Max(value = 86400, message = "durationSeconds must be at most 86400")
        Integer durationSeconds
) {
}
