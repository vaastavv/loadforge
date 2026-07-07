# LoadForge Web Dashboard

A modern, real-time dashboard for the LoadForge distributed load-testing platform.

**Stack:** React 18 · TypeScript · Vite · Material UI · Recharts · TanStack Query · Axios

## Features

- **Overview** — active workers, running tests, execution counts and recent activity at a glance.
- **Workers** — registered worker agents with live health status (active / busy / offline).
- **Tests** — currently running tests plus the full test catalogue.
- **Executions** — complete execution history with status filtering.
- **Metrics** — live throughput, latency percentiles (avg / p95 / p99) and request-outcome charts per execution.

## Getting started

```bash
cd services/web-dashboard
npm install
cp .env.example .env      # optional: adjust the API/proxy target
npm run dev               # http://localhost:5173
```

Build and preview a production bundle:

```bash
npm run build
npm run preview
```

## Configuration

| Variable | Default | Description |
| --- | --- | --- |
| `VITE_API_BASE_URL` | _(empty = same origin)_ | Base URL of the control-plane API. Leave empty in dev to use the Vite proxy. |
| `VITE_PROXY_TARGET` | `http://localhost:8080` | Dev-only: where the Vite dev server proxies `/api` requests. |

Using the proxy (the default) means no backend CORS configuration is needed for local development.

## Project structure

```
src/
├── api/          # Axios client + typed service functions (tests, executions, metrics, workers)
├── components/
│   ├── layout/   # AppLayout, Sidebar, Topbar
│   ├── common/   # StatCard, StatusChip, PageHeader, loading/error/empty states
│   ├── workers/  # WorkersTable
│   ├── tests/    # TestsTable
│   ├── executions/ # ExecutionsTable
│   └── metrics/  # Summary cards + Recharts charts
├── hooks/        # TanStack Query hooks + live metrics time-series accumulator
├── pages/        # Route-level screens (Overview, Workers, Tests, Executions, Metrics)
├── types/        # TypeScript models mirroring backend DTOs
└── utils/        # Formatting + status colour helpers
```

## Expected API

The dashboard reads exclusively from the control plane. Endpoints already implemented by the backend:

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/tests` | Create a test |
| POST | `/api/v1/tests/{id}/start` · `/stop` | Start / stop a test |
| GET | `/api/v1/tests/{id}/status` | Test status |
| GET | `/api/v1/tests/{id}/executions` | Executions for a test |
| GET | `/api/v1/metrics/executions/{executionId}` | Aggregated execution metrics |

Aggregate list endpoints the dashboard also expects the control plane to expose (each marked in the code with a `// NOTE:` comment):

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/v1/tests` | List all tests |
| GET | `/api/v1/executions` | Global execution history |
| GET | `/api/v1/workers` | Worker registry snapshot |

Until those are available, the affected views render graceful empty / error states rather than failing. Metric percentiles reflect the reported per-sample average latencies (consistent with the backend metrics aggregation notes).
