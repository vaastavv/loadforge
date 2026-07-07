-- Links an execution to the worker currently responsible for running it, enabling failover:
-- when a worker is reported OFFLINE, the control plane finds its RUNNING executions by this
-- column and reassigns them to a healthy worker. Nullable because an execution may be created
-- before a live worker is available.
ALTER TABLE executions
    ADD COLUMN assigned_worker_id UUID;

CREATE INDEX idx_executions_assigned_worker_status
    ON executions (assigned_worker_id, status);
