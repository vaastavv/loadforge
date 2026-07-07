import { Box, Chip, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import type { Test } from '@/types/api';
import { formatDateTime, formatDuration, formatNumber } from '@/utils/format';

export default function TestsTable({ tests }: { tests: Test[] }) {
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
          </TableRow>
        </TableHead>
        <TableBody>
          {tests.map((test) => (
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
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
