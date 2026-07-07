import { useQuery } from '@tanstack/react-query';
import { listTests } from '@/api/tests';

export const testsQueryKey = ['tests'] as const;

/** Loads the full test catalogue, refreshed on a slow interval. */
export function useTests(refetchIntervalMs = 15_000) {
  return useQuery({
    queryKey: testsQueryKey,
    queryFn: listTests,
    refetchInterval: refetchIntervalMs,
  });
}
