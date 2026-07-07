import { useMemo, useState } from 'react';
import { Card, CardContent, MenuItem, TextField } from '@mui/material';
import PageHeader from '@/components/common/PageHeader';
import ExecutionsTable from '@/components/executions/ExecutionsTable';
import { EmptyState, ErrorState, LoadingState } from '@/components/common/QueryStates';
import { useExecutions } from '@/hooks/useExecutions';
import { toApiError } from '@/api/client';
import type { ExecutionStatus } from '@/types/api';

const STATUS_OPTIONS: Array<ExecutionStatus | 'ALL'> = ['ALL', 'RUNNING', 'COMPLETED', 'STOPPED', 'FAILED'];

export default function ExecutionsPage() {
  const { data, isPending, isError, error, refetch } = useExecutions();
  const [status, setStatus] = useState<ExecutionStatus | 'ALL'>('ALL');

  const filtered = useMemo(() => {
    const all = [...(data ?? [])].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    );
    return status === 'ALL' ? all : all.filter((e) => e.status === status);
  }, [data, status]);

  return (
    <>
      <PageHeader
        title="Execution history"
        subtitle="Every load-test run across the platform, with status and duration."
        action={
          <TextField
            select
            size="small"
            label="Status"
            value={status}
            onChange={(e) => setStatus(e.target.value as ExecutionStatus | 'ALL')}
            sx={{ minWidth: 180 }}
          >
            {STATUS_OPTIONS.map((option) => (
              <MenuItem key={option} value={option}>
                {option}
              </MenuItem>
            ))}
          </TextField>
        }
      />

      <Card>
        <CardContent>
          {isPending ? (
            <LoadingState label="Loading executions…" />
          ) : isError ? (
            <ErrorState message={toApiError(error).message} onRetry={() => refetch()} />
          ) : filtered.length === 0 ? (
            <EmptyState title="No executions found" description="Adjust the status filter or start a new test run." />
          ) : (
            <ExecutionsTable executions={filtered} />
          )}
        </CardContent>
      </Card>
    </>
  );
}
