import { useMemo, useState } from 'react';
import { Box, Card, CardContent, Tab, Tabs } from '@mui/material';
import PageHeader from '@/components/common/PageHeader';
import TestsTable from '@/components/tests/TestsTable';
import ExecutionsTable from '@/components/executions/ExecutionsTable';
import { EmptyState, ErrorState, LoadingState } from '@/components/common/QueryStates';
import { useTests } from '@/hooks/useTests';
import { useExecutions } from '@/hooks/useExecutions';
import { toApiError } from '@/api/client';

export default function TestsPage() {
  const [tab, setTab] = useState(0);
  const tests = useTests();
  const executions = useExecutions();

  const running = useMemo(
    () => (executions.data ?? []).filter((e) => e.status === 'RUNNING'),
    [executions.data],
  );

  return (
    <>
      <PageHeader title="Tests" subtitle="Currently running load tests and your full test catalogue." />

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
    </>
  );
}
