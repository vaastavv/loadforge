import { useQuery } from '@tanstack/react-query';
import { listWorkers } from '@/api/workers';

export const workersQueryKey = ['workers'] as const;

/** Polls the worker registry snapshot on an interval for live health. */
export function useWorkers(refetchIntervalMs = 10_000) {
  return useQuery({
    queryKey: workersQueryKey,
    queryFn: listWorkers,
    refetchInterval: refetchIntervalMs,
  });
}
