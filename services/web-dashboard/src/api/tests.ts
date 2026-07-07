import { apiClient } from './client';
import type { CreateTestRequest, Execution, Test, TestStatus } from '@/types/api';

const BASE = '/api/v1/tests';

/**
 * List every defined test.
 *
 * NOTE: aggregate list endpoint expected from the control plane. See README
 * "Expected API". Renders a graceful empty state until the backend exposes it.
 */
export async function listTests(): Promise<Test[]> {
  const { data } = await apiClient.get<Test[]>(BASE);
  return data;
}

export async function createTest(payload: CreateTestRequest): Promise<Test> {
  const { data } = await apiClient.post<Test>(BASE, payload);
  return data;
}

export async function getTestStatus(testId: string): Promise<TestStatus> {
  const { data } = await apiClient.get<TestStatus>(`${BASE}/${testId}/status`);
  return data;
}

export async function startTest(testId: string): Promise<Execution> {
  const { data } = await apiClient.post<Execution>(`${BASE}/${testId}/start`);
  return data;
}

export async function stopTest(testId: string): Promise<Execution> {
  const { data } = await apiClient.post<Execution>(`${BASE}/${testId}/stop`);
  return data;
}

export async function listTestExecutions(testId: string): Promise<Execution[]> {
  const { data } = await apiClient.get<Execution[]>(`${BASE}/${testId}/executions`);
  return data;
}
