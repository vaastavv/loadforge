import { Card, CardContent, Grid } from '@mui/material';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import PowerSettingsNewIcon from '@mui/icons-material/PowerSettingsNew';
import PageHeader from '@/components/common/PageHeader';
import StatCard from '@/components/common/StatCard';
import WorkersTable from '@/components/workers/WorkersTable';
import { EmptyState, ErrorState, LoadingState } from '@/components/common/QueryStates';
import { useWorkers } from '@/hooks/useWorkers';
import { toApiError } from '@/api/client';
import type { WorkerStatus } from '@/types/api';

export default function WorkersPage() {
  const { data, isPending, isError, error, refetch } = useWorkers();
  const workers = data ?? [];
  const count = (status: WorkerStatus) => workers.filter((w) => w.status === status).length;

  return (
    <>
      <PageHeader title="Workers" subtitle="Registered worker agents and their live health status." />

      <Grid container spacing={3}>
        <Grid item xs={12} sm={4}>
          <StatCard title="Active" value={count('ACTIVE')} icon={<CheckCircleOutlineIcon />} accentColor="#16a34a" loading={isPending} />
        </Grid>
        <Grid item xs={12} sm={4}>
          <StatCard title="Busy" value={count('BUSY')} icon={<HourglassEmptyIcon />} accentColor="#f59e0b" loading={isPending} />
        </Grid>
        <Grid item xs={12} sm={4}>
          <StatCard title="Offline" value={count('OFFLINE')} icon={<PowerSettingsNewIcon />} accentColor="#94a3b8" loading={isPending} />
        </Grid>
      </Grid>

      <Card sx={{ mt: 3 }}>
        <CardContent>
          {isPending ? (
            <LoadingState label="Loading workers…" />
          ) : isError ? (
            <ErrorState message={toApiError(error).message} onRetry={() => refetch()} />
          ) : workers.length === 0 ? (
            <EmptyState
              title="No workers registered"
              description="Worker agents will appear here once they register with the control plane."
            />
          ) : (
            <WorkersTable workers={workers} />
          )}
        </CardContent>
      </Card>
    </>
  );
}
