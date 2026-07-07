import { useEffect, useMemo, useState } from 'react';
import { Autocomplete, Box, Card, CardContent, Grid, TextField } from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import PageHeader from '@/components/common/PageHeader';
import MetricsSummaryCards from '@/components/metrics/MetricsSummaryCards';
import LatencyChart from '@/components/metrics/LatencyChart';
import RequestsChart from '@/components/metrics/RequestsChart';
import ThroughputTimeSeriesChart from '@/components/metrics/ThroughputTimeSeriesChart';
import LatencyTimeSeriesChart from '@/components/metrics/LatencyTimeSeriesChart';
import { EmptyState, ErrorState, LoadingState } from '@/components/common/QueryStates';
import { useExecutions } from '@/hooks/useExecutions';
import { useExecutionMetrics } from '@/hooks/useMetrics';
import { useMetricsTimeSeries } from '@/hooks/useMetricsTimeSeries';
import { toApiError } from '@/api/client';
import type { Execution } from '@/types/api';

export default function MetricsPage() {
  const { executionId } = useParams();
  const navigate = useNavigate();
  const executions = useExecutions();
  const [selectedId, setSelectedId] = useState<string | undefined>(executionId);

  useEffect(() => {
    setSelectedId(executionId);
  }, [executionId]);

  // Default to the most recent running execution, else the most recent overall.
  useEffect(() => {
    if (selectedId || !executions.data?.length) return;
    const running = executions.data.find((e) => e.status === 'RUNNING');
    const fallback = [...executions.data].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    )[0];
    const next = (running ?? fallback)?.id;
    if (next) setSelectedId(next);
  }, [executions.data, selectedId]);

  const options = executions.data ?? [];
  const selected = useMemo(() => options.find((e) => e.id === selectedId) ?? null, [options, selectedId]);

  const metrics = useExecutionMetrics(selectedId);
  const series = useMetricsTimeSeries(selectedId, metrics.data);

  const handleChange = (_: unknown, value: Execution | null) => {
    setSelectedId(value?.id);
    navigate(value ? `/metrics/${value.id}` : '/metrics');
  };

  return (
    <>
      <PageHeader
        title="Metrics"
        subtitle="Live throughput, latency percentiles and request outcomes per execution."
        action={
          <Autocomplete
            sx={{ minWidth: 320 }}
            size="small"
            options={options}
            value={selected}
            onChange={handleChange}
            getOptionLabel={(option) => `${option.id.slice(0, 8)} · ${option.status}`}
            isOptionEqualToValue={(option, value) => option.id === value.id}
            renderInput={(params) => <TextField {...params} label="Execution" placeholder="Select an execution" />}
          />
        }
      />

      {!selectedId ? (
        <Card>
          <CardContent>
            <EmptyState
              title="Select an execution"
              description="Choose an execution above to view its live performance metrics."
            />
          </CardContent>
        </Card>
      ) : metrics.isPending ? (
        <Card>
          <CardContent>
            <LoadingState label="Loading metrics…" />
          </CardContent>
        </Card>
      ) : metrics.isError ? (
        <Card>
          <CardContent>
            <ErrorState message={toApiError(metrics.error).message} onRetry={() => metrics.refetch()} />
          </CardContent>
        </Card>
      ) : metrics.data ? (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <MetricsSummaryCards summary={metrics.data} />
          <Grid container spacing={3}>
            <Grid item xs={12} md={8}>
              <ThroughputTimeSeriesChart data={series} />
            </Grid>
            <Grid item xs={12} md={4}>
              <RequestsChart summary={metrics.data} />
            </Grid>
            <Grid item xs={12} md={8}>
              <LatencyTimeSeriesChart data={series} />
            </Grid>
            <Grid item xs={12} md={4}>
              <LatencyChart summary={metrics.data} />
            </Grid>
          </Grid>
        </Box>
      ) : null}
    </>
  );
}
