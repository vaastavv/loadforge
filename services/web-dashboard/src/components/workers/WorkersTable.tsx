import { Avatar, Box, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import DnsOutlinedIcon from '@mui/icons-material/DnsOutlined';
import type { Worker } from '@/types/api';
import StatusChip from '@/components/common/StatusChip';
import { formatDateTime } from '@/utils/format';

export default function WorkersTable({ workers }: { workers: Worker[] }) {
  return (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Worker</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Last heartbeat</TableCell>
            <TableCell>Registered</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {workers.map((worker) => (
            <TableRow key={worker.id} hover>
              <TableCell>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Avatar variant="rounded" sx={{ bgcolor: 'primary.light', width: 36, height: 36 }}>
                    <DnsOutlinedIcon fontSize="small" />
                  </Avatar>
                  <Box sx={{ minWidth: 0 }}>
                    <Typography variant="body2" fontWeight={600} noWrap>
                      {worker.hostname}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" noWrap>
                      {worker.id}
                    </Typography>
                  </Box>
                </Box>
              </TableCell>
              <TableCell>
                <StatusChip kind="worker" status={worker.status} />
              </TableCell>
              <TableCell>{formatDateTime(worker.lastHeartbeat)}</TableCell>
              <TableCell>{formatDateTime(worker.registeredAt)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
