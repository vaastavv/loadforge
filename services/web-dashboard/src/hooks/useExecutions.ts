import { useQuery } from '@tanstack/react-query';
import { listExecutions } from '@/api/executions';
import { listTestExecutions } from '@/api/tests';

export const executionsQueryKey = ['executions'] as const;

/** Global execution history, polled for live status changes. */
export function useExecutions(refetchIntervalMs = 10_000) {
  return useQuery({
    queryKey: executionsQueryKey,
    queryFn: listExecutions,
    refetchInterval: refetchIntervalMs,
  });
}

/** Executions for a single test; disabled until a testId is provided. */
export function useTestExecutions(testId: string | undefined) {
  return useQuery({
    queryKey: ['executions', 'byTest', testId],
    queryFn: () => listTestExecutions(testId as string),
    enabled: Boolean(testId),
  });
}
