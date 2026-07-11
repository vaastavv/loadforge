/**
 * TypeScript models mirroring the LoadForge backend DTOs
 * (test-management-service + worker-service).
 */

export type UUID = string;

/** ISO-8601 timestamp string (serialized Java `Instant`). */
export type IsoDateTime = string;

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'HEAD' | 'OPTIONS';

export type ExecutionStatus = 'RUNNING' | 'STOPPED' | 'COMPLETED' | 'FAILED';

export type WorkerStatus = 'ACTIVE' | 'BUSY' | 'OFFLINE';

export interface Test {
  id: UUID;
  name: string;
  description: string | null;
  targetUrl: string;
  httpMethod: HttpMethod;
  virtualUsers: number;
  durationSeconds: number;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface CreateTestRequest {
  name: string;
  description?: string;
  targetUrl: string;
  httpMethod: HttpMethod;
  virtualUsers: number;
  durationSeconds: number;
}

export interface RegisterWorkerRequest {
  hostname: string;
}

export interface Execution {
  id: UUID;
  testId: UUID;
  status: ExecutionStatus;
  startedAt: IsoDateTime | null;
  finishedAt: IsoDateTime | null;
  errorMessage: string | null;
  createdAt: IsoDateTime;
}

export interface TestStatus {
  testId: UUID;
  testName: string;
  status: string;
  activeExecutionId: UUID | null;
  since: IsoDateTime;
}

export interface ExecutionMetricsSummary {
  executionId: UUID;
  totalRequests: number;
  successfulRequests: number;
  failures: number;
  requestsPerSecond: number;
  throughput: number;
  averageLatencyMs: number;
  p95LatencyMs: number;
  p99LatencyMs: number;
  sampleCount: number;
  windowStart: IsoDateTime | null;
  windowEnd: IsoDateTime | null;
  windowSeconds: number;
}

export interface Worker {
  id: UUID;
  hostname: string;
  status: WorkerStatus;
  lastHeartbeat: IsoDateTime | null;
  registeredAt: IsoDateTime | null;
}
