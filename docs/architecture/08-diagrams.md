# 08 — UML & Sequence Diagrams

All diagrams are Mermaid so they render in GitHub/VS Code and stay version-controlled.

---

## 1. Domain class diagram (full)

```mermaid
classDiagram
    direction LR

    class Organization {
        +UUID id
        +String name
        +String slug
    }
    class Project {
        +UUID id
        +UUID orgId
        +String name
        +String slug
    }
    class TestDefinition {
        +UUID id
        +UUID projectId
        +String name
        +int version
        +ExecutorType executorType
        +boolean archived
    }
    class HttpRequestSpec {
        <<value object>>
        +HttpMethod method
        +String url
        +Map~String,String~ headers
        +String body
        +int timeoutMs
    }
    class LoadProfile {
        <<value object>>
        +int startVus
        +int maxVus
        +List~Stage~ stages
        +int gracefulStopSec
        +totalDurationSec() int
        +peakTarget() int
    }
    class Stage {
        <<value object>>
        +int durationSec
        +int target
    }
    class Threshold {
        <<value object>>
        +String metric
        +String aggregation
        +Comparator comparator
        +double value
    }
    class TestRun {
        +UUID id
        +RunStatus status
        +int requestedVus
        +int requestedWorkers
        +Instant startedAt
        +Instant endedAt
        +markRunning()
        +abort(reason)
        +complete()
    }
    class TestRunShard {
        +UUID id
        +int shardIndex
        +int assignedVus
        +ShardStatus status
    }
    class Worker {
        +UUID id
        +WorkerStatus status
        +int capacityVus
        +Instant lastHeartbeatAt
        +isHealthy(now) boolean
    }
    class RunSummary {
        +UUID testRunId
        +long totalRequests
        +double errorRate
        +double p95LatencyMs
        +double throughputRps
        +boolean passed
    }
    class Schedule {
        +UUID id
        +String cronExpression
        +String timezone
        +boolean enabled
    }

    Organization "1" o-- "*" Project
    Project "1" o-- "*" TestDefinition
    TestDefinition "1" *-- "1" HttpRequestSpec
    TestDefinition "1" *-- "1" LoadProfile
    LoadProfile "1" *-- "*" Stage
    TestDefinition "1" *-- "*" Threshold
    TestDefinition "1" --> "*" TestRun : launched as
    TestDefinition "1" --> "*" Schedule
    TestRun "1" *-- "*" TestRunShard
    TestRun "1" --> "1" RunSummary
    Worker "1" --> "*" TestRunShard : executes
```

---

## 2. TestRun state machine

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> QUEUED : validated
    PENDING --> FAILED : validation error
    QUEUED --> PROVISIONING : capacity reserved
    QUEUED --> ABORTED : cancelled before start
    QUEUED --> FAILED : no capacity
    PROVISIONING --> RUNNING : all shards started
    PROVISIONING --> ABORTING : abort requested
    PROVISIONING --> FAILED : shard start failure
    RUNNING --> COMPLETED : all shards completed
    RUNNING --> FAILED : shard failure / lost worker
    RUNNING --> ABORTING : abort requested
    ABORTING --> ABORTED : all shards stopped
    ABORTING --> FAILED : stop error
    COMPLETED --> [*]
    FAILED --> [*]
    ABORTED --> [*]
```

## 3. Worker state machine

```mermaid
stateDiagram-v2
    [*] --> REGISTERING
    REGISTERING --> IDLE : registered + first heartbeat
    IDLE --> BUSY : shard assigned
    BUSY --> IDLE : shard finished
    IDLE --> DRAINING : cordon requested
    BUSY --> DRAINING : cordon requested
    DRAINING --> OFFLINE : in-flight drained
    IDLE --> OFFLINE : heartbeat timeout
    BUSY --> OFFLINE : heartbeat timeout (shards LOST)
    OFFLINE --> REGISTERING : re-join
    OFFLINE --> [*]
```

---

## 4. Sequence — Create & validate a test

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant GW as API Gateway
    participant CP as Control Plane
    participant DB as PostgreSQL

    U->>GW: POST /projects/{id}/tests (definition)
    GW->>GW: validate JWT, resolve org/role (EDITOR+)
    GW->>CP: create test definition
    CP->>CP: validate loadProfile, thresholds, requestSpec
    CP->>CP: SSRF check on target URL
    CP->>DB: INSERT test_definitions (version=1)
    DB-->>CP: id
    CP-->>GW: 201 Created {id}
    GW-->>U: 201 + Location
    U->>GW: POST /tests/{id}/validate
    GW->>CP: render k6 script (dry run)
    CP-->>U: 200 {renderedScript, estimatedRps}
```

---

## 5. Sequence — Launch distributed run (orchestration core)

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant GW as Gateway
    participant CP as Control Plane
    participant RD as Redis
    participant DB as PostgreSQL
    participant K as Kafka
    participant W as Worker Agents

    U->>GW: POST /tests/{id}/run {vus:500, workers:4}
    GW->>CP: launchRun
    CP->>RD: acquire lock run:{testId}
    CP->>DB: load healthy workers (last_heartbeat < 15s)
    CP->>CP: VuShardingService.plan(500, 4, workers)
    alt insufficient capacity
        CP-->>U: 409 Conflict (InsufficientCapacity)
    else capacity ok
        CP->>DB: INSERT test_run(status=QUEUED) + shards(ASSIGNED)
        CP->>DB: UPDATE workers SET status=BUSY, current_run_id
        CP->>CP: TestRun.assignShards -> PROVISIONING
        loop each shard
            CP->>K: publish JobAssigned -> test.jobs [key=runId]
        end
        CP->>RD: release lock
        CP-->>U: 202 Accepted {runId, status:PROVISIONING}
        K-->>W: deliver shard (consumer group)
        W->>W: render k6 script from JobAssigned
        W->>K: ShardLifecycle(STARTING) -> test.run.events
        W->>W: exec k6 at startAtEpochMs (synchronized)
        W->>K: ShardLifecycle(RUNNING)
        K-->>CP: shards RUNNING
        CP->>CP: TestRun.markRunning -> RUNNING
        CP->>DB: UPDATE test_run status=RUNNING
    end
```

---

## 6. Sequence — Metrics pipeline & live dashboard

```mermaid
sequenceDiagram
    autonumber
    participant W as Worker Agent
    participant K1 as Kafka test.metrics.raw
    participant MS as Metrics Service
    participant DB as TimescaleDB
    participant K2 as Kafka test.metrics.aggregated
    participant CP as Control Plane
    participant UI as React SPA

    Note over W: k6 emits samples; agent pre-aggregates per 1s (t-digest)
    loop every 1s window
        W->>K1: MetricSampleBatch(runId, workerId, seq)
    end
    K1-->>MS: consume batches (group metrics-agg)
    MS->>MS: merge all workers for window (tumbling 1s)
    MS->>DB: upsert metric_samples ON CONFLICT DO UPDATE
    MS->>MS: evaluate thresholds (streaming)
    MS->>K2: MetricsAggregated(window)
    alt threshold breached
        MS->>CP: NotificationRequested -> notifications
    end
    K2-->>CP: MetricsAggregated
    CP-->>UI: SSE event: metrics (live charts)
    UI->>UI: append point to Recharts series
```

---

## 7. Sequence — Worker registration & heartbeat

```mermaid
sequenceDiagram
    autonumber
    participant W as Worker Agent
    participant CP as Control Plane
    participant DB as PostgreSQL
    participant K as Kafka worker.heartbeat

    Note over W: on startup
    W->>CP: POST /internal/workers/register {hostname, capacityVus, labels}
    CP->>DB: UPSERT workers(status=REGISTERING)
    CP-->>W: 200 {workerId}
    loop every 5s
        W->>K: WorkerHeartbeat(status, cpu, mem, capacity)
    end
    K-->>CP: consume heartbeats (group cp-heartbeat)
    CP->>DB: UPDATE workers SET last_heartbeat_at, status, metrics
    Note over CP: reaper job every 5s
    CP->>DB: SELECT workers WHERE last_heartbeat_at < now()-15s
    CP->>DB: UPDATE stale workers SET status=OFFLINE
    CP->>CP: mark their shards LOST -> re-shard or fail run
```

---

## 8. Sequence — Abort a running test

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant GW as Gateway
    participant CP as Control Plane
    participant K as Kafka test.commands
    participant W as Worker Agents
    participant DB as PostgreSQL

    U->>GW: POST /runs/{id}/abort
    GW->>CP: abortRun(runId)
    CP->>CP: TestRun.abort() -> ABORTING
    CP->>DB: UPDATE test_run status=ABORTING
    CP->>K: RunCommand(ABORT, runId)
    CP-->>U: 202 Accepted
    K-->>W: deliver ABORT (all shards of runId)
    W->>W: send SIGTERM to k6, graceful stop
    W->>K: ShardLifecycle(ABORTED) -> test.run.events
    K-->>CP: shards ABORTED
    CP->>CP: all terminal -> TestRun -> ABORTED
    CP->>DB: UPDATE test_run status=ABORTED, ended_at
    CP->>DB: UPDATE workers status=IDLE
```

---

## 9. Sequence — Scheduled run trigger

```mermaid
sequenceDiagram
    autonumber
    participant Q as Quartz Scheduler
    participant CP as Control Plane
    participant DB as PostgreSQL
    participant K as Kafka

    Note over Q: cron fires (Postgres job store, clustered)
    Q->>CP: trigger schedule {scheduleId}
    CP->>DB: load test_definition + compute requestedVus/workers
    CP->>CP: launchRun(triggerType=SCHEDULED)
    CP->>K: JobAssigned[] -> test.jobs
    CP->>DB: UPDATE schedules SET last_run_at, next_run_at
```

---

## 10. Deployment view (Kubernetes)

```mermaid
flowchart TB
    subgraph ingress[Ingress / LB]
        ing[NGINX Ingress + TLS]
    end
    subgraph ns_app[Namespace: loadforge]
        gw[Deployment: api-gateway<br/>HPA 2-6]
        cp[Deployment: control-plane<br/>HPA 2-4]
        ms[Deployment: metrics-service<br/>HPA 2-8]
        wa[Deployment: worker-agent<br/>HPA 2-50]
        ns[Deployment: notification-service]
        fe[Deployment: web-app<br/>nginx static]
    end
    subgraph ns_data[Namespace: data]
        kafka[StatefulSet: Kafka x3]
        pg[StatefulSet: Postgres/Timescale + PVC]
        redis[Deployment: Redis]
        kc[Deployment: Keycloak]
    end
    subgraph ns_obs[Namespace: observability]
        prom[Prometheus]
        graf[Grafana]
        otel[OTel Collector]
        loki[Loki]
    end

    ing --> gw
    ing --> fe
    gw --> cp
    gw --> ms
    cp --> kafka
    cp --> pg
    cp --> redis
    ms --> kafka
    ms --> pg
    wa --> kafka
    ns --> kafka
    cp -.-> kc
    prom -.scrape.-> gw & cp & ms & wa
    otel --> loki
    prom --> graf
```
