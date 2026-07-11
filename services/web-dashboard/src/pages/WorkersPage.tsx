import { useState } from 'react';
import { Button, Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Stack, TextField } from '@mui/material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import PowerSettingsNewIcon from '@mui/icons-material/PowerSettingsNew';
import PageHeader from '@/components/common/PageHeader';
import StatCard from '@/components/common/StatCard';
import WorkersTable from '@/components/workers/WorkersTable';
import { EmptyState, ErrorState, LoadingState } from '@/components/common/QueryStates';
import { useWorkers, workersQueryKey } from '@/hooks/useWorkers';
import { toApiError } from '@/api/client';
import { registerWorker } from '@/api/workers';
import type { RegisterWorkerRequest, WorkerStatus } from '@/types/api';

export default function WorkersPage() {
  const [open, setOpen] = useState(false);
  const [hostname, setHostname] = useState('');
  const { data, isPending, isError, error, refetch } = useWorkers();
  const workers = data ?? [];
  const count = (status: WorkerStatus) => workers.filter((w) => w.status === status).length;
  const queryClient = useQueryClient();

  const registerMutation = useMutation({
    mutationFn: (payload: RegisterWorkerRequest) => registerWorker(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: workersQueryKey });
      setOpen(false);
      setHostname('');
    },
  });

  return (
    <>
      <PageHeader
        title="Workers"
        subtitle="Registered worker agents and their live health status."
        action={
          <Button variant="contained" onClick={() => setOpen(true)}>
            Register worker
          </Button>
        }
      />

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

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Register worker</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Hostname"
              value={hostname}
              onChange={(event) => setHostname(event.target.value)}
              placeholder="worker-01"
              required
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => registerMutation.mutate({ hostname: hostname.trim() })} disabled={registerMutation.isPending || !hostname.trim()}>
            {registerMutation.isPending ? 'Registering…' : 'Register'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
