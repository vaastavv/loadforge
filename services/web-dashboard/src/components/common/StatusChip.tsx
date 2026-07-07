import { Chip } from '@mui/material';
import FiberManualRecordIcon from '@mui/icons-material/FiberManualRecord';
import type { ExecutionStatus, WorkerStatus } from '@/types/api';
import { executionStatusColor, workerStatusColor } from '@/utils/status';

interface StatusChipProps {
  status: ExecutionStatus | WorkerStatus;
  kind: 'execution' | 'worker';
}

export default function StatusChip({ status, kind }: StatusChipProps) {
  const color =
    kind === 'execution'
      ? executionStatusColor(status as ExecutionStatus)
      : workerStatusColor(status as WorkerStatus);

  return (
    <Chip
      size="small"
      color={color}
      variant="outlined"
      icon={<FiberManualRecordIcon sx={{ fontSize: 12 }} />}
      label={status}
      sx={{ letterSpacing: 0.3 }}
    />
  );
}
