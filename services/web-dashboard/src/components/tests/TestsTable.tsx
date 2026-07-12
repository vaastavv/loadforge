import { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Snackbar,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { Test } from '@/types/api';
import { startTest } from '@/api/tests';
import { testsQueryKey } from '@/hooks/useTests';
import { executionsQueryKey } from '@/hooks/useExecutions';
import { formatDateTime, formatDuration, formatNumber } from '@/utils/format';

export default function TestsTable({ tests }: { tests: Test[] }) {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const startMutation = useMutation({
    mutationFn: startTest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: testsQueryKey });
      queryClient.invalidateQueries({ queryKey: executionsQueryKey });
    },
    onError: (err) => {
      setError(err instanceof Error ? err.message : 'Failed to start test');
    },
  });

  return (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Method</TableCell>
            <TableCell>Target URL</TableCell>
            <TableCell align="right">Virtual users</TableCell>
            <TableCell align="right">Duration</TableCell>
            <TableCell>Created</TableCell>
            <TableCell align="right">Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {tests.map((test) => {
            const isStarting = startMutation.isPending && startMutation.variables === test.id;
            return (
              <TableRow key={test.id} hover>
                <TableCell>
                  <Typography variant="body2" fontWeight={600}>
                    {test.name}
                  </Typography>
                  {test.description && (
                    <Typography variant="caption" color="text.secondary">
                      {test.description}
                    </Typography>
                  )}
                </TableCell>
                <TableCell>
                  <Chip size="small" label={test.httpMethod} variant="outlined" />
                </TableCell>
                <TableCell>
                  <Box
                    sx={{ maxWidth: 280, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                    title={test.targetUrl}
                  >
                    {test.targetUrl}
                  </Box>
                </TableCell>
                <TableCell align="right">{formatNumber(test.virtualUsers)}</TableCell>
                <TableCell align="right">{formatDuration(test.durationSeconds)}</TableCell>
                <TableCell>{formatDateTime(test.createdAt)}</TableCell>
                <TableCell align="right">
                  <Button
                    size="small"
                    variant="contained"
                    startIcon={
                      isStarting ? <CircularProgress size={14} color="inherit" /> : <PlayArrowIcon />
                    }
                    onClick={() => startMutation.mutate(test.id)}
                    disabled={startMutation.isPending}
                  >
                    Start
                  </Button>
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
      <Snackbar
        open={Boolean(error)}
        autoHideDuration={6000}
        onClose={() => setError(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert severity="error" variant="filled" onClose={() => setError(null)}>
          {error}
        </Alert>
      </Snackbar>
    </TableContainer>
  );
}
