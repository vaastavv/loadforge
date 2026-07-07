import { apiClient } from './client';
import type { ExecutionMetricsSummary } from '@/types/api';

/** Fetch the aggregated performance metrics for a single execution. */
export async function getExecutionMetrics(executionId: string): Promise<ExecutionMetricsSummary> {
  const { data } = await apiClient.get<ExecutionMetricsSummary>(
    `/api/v1/metrics/executions/${executionId}`,
  );
  return data;
}
