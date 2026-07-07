import { apiClient } from './client';
import type { Execution } from '@/types/api';

/**
 * List every execution across all tests (global history).
 *
 * NOTE: aggregate list endpoint expected from the control plane. See README
 * "Expected API". Renders a graceful empty state until the backend exposes it.
 */
export async function listExecutions(): Promise<Execution[]> {
  const { data } = await apiClient.get<Execution[]>('/api/v1/executions');
  return data;
}
