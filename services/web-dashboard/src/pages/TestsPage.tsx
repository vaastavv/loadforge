import { useMemo, useState } from 'react';
import { Box, Button, Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack, Tab, Tabs, TextField } from '@mui/material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import PageHeader from '@/components/common/PageHeader';
import TestsTable from '@/components/tests/TestsTable';
import ExecutionsTable from '@/components/executions/ExecutionsTable';
import { EmptyState, ErrorState, LoadingState } from '@/components/common/QueryStates';
import { useTests, testsQueryKey } from '@/hooks/useTests';
import { useExecutions, executionsQueryKey } from '@/hooks/useExecutions';
import { toApiError } from '@/api/client';
import { createTest } from '@/api/tests';
import type { CreateTestRequest } from '@/types/api';

export default function TestsPage() {
  const [tab, setTab] = useState(0);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<CreateTestRequest>({
    name: '',
    description: '',
    targetUrl: 'https://example.com',
    httpMethod: 'GET',
    virtualUsers: 10,
    durationSeconds: 30,
  });
  const tests = useTests();
  const executions = useExecutions();
  const queryClient = useQueryClient();

  const createMutation = useMutation({
    mutationFn: createTest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: testsQueryKey });
      queryClient.invalidateQueries({ queryKey: executionsQueryKey });
      setOpen(false);
      setForm((current) => ({ ...current, name: '', description: '', targetUrl: 'https://example.com' }));
    },
  });

  const running = useMemo(
    () => (executions.data ?? []).filter((e) => e.status === 'RUNNING'),
    [executions.data],
  );

  const handleSubmit = () => {
    createMutation.mutate({
      ...form,
      description: form.description?.trim() ? form.description.trim() : undefined,
    });
  };

  return (
    <>
      <PageHeader
        title="Tests"
        subtitle="Currently running load tests and your full test catalogue."
        action={
          <Button variant="contained" onClick={() => setOpen(true)}>
            Add test
          </Button>
        }
      />

      <Card>
        <Tabs
          value={tab}
          onChange={(_, value) => setTab(value)}
          sx={{ px: 2, borderBottom: '1px solid', borderColor: 'divider' }}
        >
          <Tab label={`Running (${running.length})`} />
          <Tab label={`All tests (${tests.data?.length ?? 0})`} />
        </Tabs>
        <CardContent>
          <Box hidden={tab !== 0}>
            {executions.isPending ? (
              <LoadingState />
            ) : executions.isError ? (
              <ErrorState message={toApiError(executions.error).message} onRetry={() => executions.refetch()} />
            ) : running.length === 0 ? (
              <EmptyState title="No tests running" description="Running executions will appear here in real time." />
            ) : (
              <ExecutionsTable executions={running} />
            )}
          </Box>
          <Box hidden={tab !== 1}>
            {tests.isPending ? (
              <LoadingState />
            ) : tests.isError ? (
              <ErrorState message={toApiError(tests.error).message} onRetry={() => tests.refetch()} />
            ) : (tests.data ?? []).length === 0 ? (
              <EmptyState title="No tests defined" description="Create a load test to get started." />
            ) : (
              <TestsTable tests={tests.data ?? []} />
            )}
          </Box>
        </CardContent>
      </Card>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Create test</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Name"
              value={form.name}
              onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
              required
            />
            <TextField
              label="Description"
              value={form.description ?? ''}
              onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
              multiline
              minRows={2}
            />
            <TextField
              label="Target URL"
              value={form.targetUrl}
              onChange={(event) => setForm((current) => ({ ...current, targetUrl: event.target.value }))}
              required
            />
            <TextField
              select
              label="HTTP method"
              value={form.httpMethod}
              onChange={(event) => setForm((current) => ({ ...current, httpMethod: event.target.value as CreateTestRequest['httpMethod'] }))}
            >
              {['GET', 'POST', 'PUT', 'PATCH', 'DELETE'].map((method) => (
                <MenuItem key={method} value={method}>
                  {method}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Virtual users"
              type="number"
              value={form.virtualUsers}
              onChange={(event) => setForm((current) => ({ ...current, virtualUsers: Number(event.target.value) }))}
              inputProps={{ min: 1 }}
            />
            <TextField
              label="Duration (seconds)"
              type="number"
              value={form.durationSeconds}
              onChange={(event) => setForm((current) => ({ ...current, durationSeconds: Number(event.target.value) }))}
              inputProps={{ min: 1 }}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit} disabled={createMutation.isPending || !form.name.trim() || !form.targetUrl.trim()}>
            {createMutation.isPending ? 'Creating…' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
