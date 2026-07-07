CREATE TABLE metric_samples (
    id                  UUID             PRIMARY KEY,
    execution_id        UUID             NOT NULL,
    worker_id           UUID             NOT NULL,
    total_requests      BIGINT           NOT NULL,
    successful_requests BIGINT           NOT NULL,
    failed_requests     BIGINT           NOT NULL,
    average_latency_ms  DOUBLE PRECISION NOT NULL,
    recorded_at         TIMESTAMPTZ      NOT NULL,
    received_at         TIMESTAMPTZ      NOT NULL DEFAULT now()
);

CREATE INDEX idx_metric_samples_execution_id ON metric_samples (execution_id);
CREATE INDEX idx_metric_samples_recorded_at  ON metric_samples (recorded_at);
