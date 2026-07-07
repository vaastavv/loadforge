import axios, { AxiosError } from 'axios';
import { API_BASE_URL } from './config';

/** Shared Axios instance for all control-plane API calls. */
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
});

export interface ApiErrorShape {
  status?: number;
  message: string;
}

/** Normalizes any thrown value into a display-friendly API error. */
export function toApiError(error: unknown): ApiErrorShape {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ message?: string; error?: string }>;
    const status = axiosError.response?.status;
    const message =
      axiosError.response?.data?.message ??
      axiosError.response?.data?.error ??
      axiosError.message ??
      'Request failed';
    return { status, message };
  }
  return { message: error instanceof Error ? error.message : 'Unknown error' };
}
