import { useQuery } from '@tanstack/react-query';
import { getExecutionMetrics } from '@/api/metrics';

export function metricsQueryKey(executionId: string | undefined) {
  return ['metrics', executionId] as const;
}

/**
 * Polls aggregated metrics for an execution at a fast cadence so throughput and
 * latency stay near real-time. Disabled until an executionId is selected.
 */
export function useExecutionMetrics(executionId: string | undefined, refetchIntervalMs = 5_000) {
  return useQuery({
    queryKey: metricsQueryKey(executionId),
    queryFn: () => getExecutionMetrics(executionId as string),
    enabled: Boolean(executionId),
    refetchInterval: refetchIntervalMs,
  });
}
