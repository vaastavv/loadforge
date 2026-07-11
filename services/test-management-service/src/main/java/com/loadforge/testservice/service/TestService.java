package com.loadforge.testservice.service;

import com.loadforge.testservice.dto.CreateTestRequest;
import com.loadforge.testservice.dto.ExecutionResponse;
import com.loadforge.testservice.dto.TestResponse;
import com.loadforge.testservice.dto.TestStatusResponse;

import java.util.List;
import java.util.UUID;

public interface TestService {

    TestResponse createTest(CreateTestRequest request);

    ExecutionResponse startTest(UUID testId);

    ExecutionResponse stopTest(UUID testId);

    TestStatusResponse getTestStatus(UUID testId);

    List<TestResponse> listTests();

    List<ExecutionResponse> listAllExecutions();

    List<ExecutionResponse> listExecutions(UUID testId);
}
