import { useEffect, useRef, useState } from 'react';
import type { ExecutionMetricsSummary } from '@/types/api';

export interface MetricsTimePoint {
  timestamp: number;
  label: string;
  requestsPerSecond: number;
  throughput: number;
  averageLatencyMs: number;
  p95LatencyMs: number;
  p99LatencyMs: number;
}

const MAX_POINTS = 60;

/**
 * Accumulates successive metric summaries into a bounded, in-memory time series
 * so live rates and latencies can be charted over time. The series resets when
 * the selected execution changes, and duplicate samples (same window + count)
 * are ignored.
 */
export function useMetricsTimeSeries(
  executionId: string | undefined,
  summary: ExecutionMetricsSummary | undefined,
): MetricsTimePoint[] {
  const [series, setSeries] = useState<MetricsTimePoint[]>([]);
  const lastKeyRef = useRef<string | null>(null);

  useEffect(() => {
    setSeries([]);
    lastKeyRef.current = null;
  }, [executionId]);

  useEffect(() => {
    if (!summary) return;

    const key = `${summary.windowEnd ?? ''}:${summary.sampleCount}`;
    if (key === lastKeyRef.current) return;
    lastKeyRef.current = key;

    const now = new Date();
    const point: MetricsTimePoint = {
      timestamp: now.getTime(),
      label: now.toLocaleTimeString(),
      requestsPerSecond: Number(summary.requestsPerSecond.toFixed(2)),
      throughput: Number(summary.throughput.toFixed(2)),
      averageLatencyMs: Number(summary.averageLatencyMs.toFixed(2)),
      p95LatencyMs: Number(summary.p95LatencyMs.toFixed(2)),
      p99LatencyMs: Number(summary.p99LatencyMs.toFixed(2)),
    };
    setSeries((prev) => [...prev, point].slice(-MAX_POINTS));
  }, [summary]);

  return series;
}
