import type { ExecutionStatus, WorkerStatus } from '@/types/api';

export type MuiStatusColor =
  | 'default'
  | 'primary'
  | 'secondary'
  | 'success'
  | 'error'
  | 'info'
  | 'warning';

export function executionStatusColor(status: ExecutionStatus): MuiStatusColor {
  switch (status) {
    case 'RUNNING':
      return 'info';
    case 'COMPLETED':
      return 'success';
    case 'STOPPED':
      return 'warning';
    case 'FAILED':
      return 'error';
    default:
      return 'default';
  }
}

export function workerStatusColor(status: WorkerStatus): MuiStatusColor {
  switch (status) {
    case 'ACTIVE':
      return 'success';
    case 'BUSY':
      return 'warning';
    case 'OFFLINE':
      return 'default';
    default:
      return 'default';
  }
}
