import { Card, CardContent, CardHeader, Grid } from '@mui/material';
import DnsOutlinedIcon from '@mui/icons-material/DnsOutlined';
import PlayCircleOutlineIcon from '@mui/icons-material/PlayCircleOutline';
import ScienceOutlinedIcon from '@mui/icons-material/ScienceOutlined';
import HistoryOutlinedIcon from '@mui/icons-material/HistoryOutlined';
import PageHeader from '@/components/common/PageHeader';
import StatCard from '@/components/common/StatCard';
import { EmptyState, ErrorState, LoadingState } from '@/components/common/QueryStates';
import WorkersTable from '@/components/workers/WorkersTable';
import ExecutionsTable from '@/components/executions/ExecutionsTable';
import { useWorkers } from '@/hooks/useWorkers';
import { useTests } from '@/hooks/useTests';
import { useExecutions } from '@/hooks/useExecutions';
import { toApiError } from '@/api/client';

export default function DashboardPage() {
  const workers = useWorkers();
  const tests = useTests();
  const executions = useExecutions();

  const activeWorkers = (workers.data ?? []).filter((w) => w.status === 'ACTIVE').length;
  const runningExecutions = (executions.data ?? []).filter((e) => e.status === 'RUNNING');
  const recentExecutions = [...(executions.data ?? [])]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 8);

  return (
    <>
      <PageHeader title="Overview" subtitle="Fleet health, active load tests and recent execution activity." />

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Active workers"
            value={activeWorkers}
            icon={<DnsOutlinedIcon />}
            accentColor="#16a34a"
            loading={workers.isPending}
            subtitle={`${workers.data?.length ?? 0} registered`}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Running tests"
            value={runningExecutions.length}
            icon={<PlayCircleOutlineIcon />}
            accentColor="#4f46e5"
            loading={executions.isPending}
            subtitle="currently executing"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total tests"
            value={tests.data?.length ?? 0}
            icon={<ScienceOutlinedIcon />}
            accentColor="#0ea5e9"
            loading={tests.isPending}
            subtitle="defined scenarios"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Executions"
            value={executions.data?.length ?? 0}
            icon={<HistoryOutlinedIcon />}
            accentColor="#f59e0b"
            loading={executions.isPending}
            subtitle="all-time runs"
          />
        </Grid>

        <Grid item xs={12} md={7}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Recent executions" titleTypographyProps={{ variant: 'h6' }} />
            <CardContent sx={{ pt: 0 }}>
              {executions.isPending ? (
                <LoadingState />
              ) : executions.isError ? (
                <ErrorState message={toApiError(executions.error).message} onRetry={() => executions.refetch()} />
              ) : recentExecutions.length === 0 ? (
                <EmptyState title="No executions yet" description="Start a test to see execution activity here." />
              ) : (
                <ExecutionsTable executions={recentExecutions} />
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={5}>
          <Card sx={{ height: '100%' }}>
            <CardHeader title="Workers" titleTypographyProps={{ variant: 'h6' }} />
            <CardContent sx={{ pt: 0 }}>
              {workers.isPending ? (
                <LoadingState />
              ) : workers.isError ? (
                <ErrorState message={toApiError(workers.error).message} onRetry={() => workers.refetch()} />
              ) : (workers.data ?? []).length === 0 ? (
                <EmptyState title="No workers registered" />
              ) : (
                <WorkersTable workers={(workers.data ?? []).slice(0, 6)} />
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </>
  );
}
