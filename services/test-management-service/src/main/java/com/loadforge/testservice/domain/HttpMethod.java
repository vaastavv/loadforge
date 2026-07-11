package com.loadforge.testservice.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HttpMethod {
    GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS;

    @JsonCreator
    public static HttpMethod fromString(String value) {
        if (value == null) {
            return null;
        }
        return HttpMethod.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
