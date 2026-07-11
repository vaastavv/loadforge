import { apiClient } from './client';
import type { RegisterWorkerRequest, Worker } from '@/types/api';

/**
 * Snapshot of the worker registry.
 *
 * NOTE: aggregate list endpoint expected from the control plane. See README
 * "Expected API". Renders a graceful empty state until the backend exposes it.
 */
export async function listWorkers(): Promise<Worker[]> {
  const { data } = await apiClient.get<Worker[]>('/api/v1/workers');
  return data;
}

export async function registerWorker(payload: RegisterWorkerRequest): Promise<Worker> {
  const { data } = await apiClient.post<Worker>('/worker-api/register', payload);
  return data;
}
