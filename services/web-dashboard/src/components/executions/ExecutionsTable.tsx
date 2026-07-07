import { IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography } from '@mui/material';
import InsightsOutlinedIcon from '@mui/icons-material/InsightsOutlined';
import { useNavigate } from 'react-router-dom';
import type { Execution } from '@/types/api';
import StatusChip from '@/components/common/StatusChip';
import { elapsedSeconds, formatDateTime, formatDuration } from '@/utils/format';

interface ExecutionsTableProps {
  executions: Execution[];
  showMetricsLink?: boolean;
}

export default function ExecutionsTable({ executions, showMetricsLink = true }: ExecutionsTableProps) {
  const navigate = useNavigate();

  return (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Execution</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Started</TableCell>
            <TableCell>Finished</TableCell>
            <TableCell align="right">Duration</TableCell>
            {showMetricsLink && <TableCell align="right">Metrics</TableCell>}
          </TableRow>
        </TableHead>
        <TableBody>
          {executions.map((execution) => {
            const duration =
              execution.status === 'RUNNING'
                ? elapsedSeconds(execution.startedAt)
                : elapsedSeconds(execution.startedAt, execution.finishedAt);

            return (
              <TableRow key={execution.id} hover>
                <TableCell>
                  <Typography variant="body2" fontWeight={600}>
                    {execution.id.slice(0, 8)}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    test {execution.testId.slice(0, 8)}
                  </Typography>
                </TableCell>
                <TableCell>
                  <StatusChip kind="execution" status={execution.status} />
                </TableCell>
                <TableCell>{formatDateTime(execution.startedAt)}</TableCell>
                <TableCell>{formatDateTime(execution.finishedAt)}</TableCell>
                <TableCell align="right">{formatDuration(duration)}</TableCell>
                {showMetricsLink && (
                  <TableCell align="right">
                    <Tooltip title="View metrics">
                      <IconButton size="small" onClick={() => navigate(`/metrics/${execution.id}`)} aria-label="View metrics">
                        <InsightsOutlinedIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                )}
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
