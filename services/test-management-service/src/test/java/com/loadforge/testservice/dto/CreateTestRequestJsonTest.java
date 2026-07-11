package com.loadforge.testservice.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTestRequestJsonTest {

    @Test
    void shouldDeserializeCreateTestRequestWithHttpMethodString() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String json = """
                {
                  "name": "smoke-test",
                  "description": "smoke",
                  "targetUrl": "https://example.com",
                  "httpMethod": "GET",
                  "virtualUsers": 1,
                  "durationSeconds": 10
                }
                """;

        CreateTestRequest request = objectMapper.readValue(json, CreateTestRequest.class);

        assertThat(request.name()).isEqualTo("smoke-test");
        assertThat(request.httpMethod()).isEqualTo(com.loadforge.testservice.domain.HttpMethod.GET);
        assertThat(request.targetUrl()).isEqualTo("https://example.com");
    }
}
