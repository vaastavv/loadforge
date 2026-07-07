-- SLA validation module.
-- sla_policies:            named threshold sets (the active one drives validation).
-- sla_validation_results:  the overall PASS/FAIL verdict recorded per execution.
-- sla_rule_results:        the per-rule breakdown produced by the rules engine.

CREATE TABLE sla_policies (
    id                     UUID             PRIMARY KEY,
    name                   VARCHAR(100)     NOT NULL UNIQUE,
    max_p95_latency_ms     DOUBLE PRECISION NOT NULL,
    max_error_rate_percent DOUBLE PRECISION NOT NULL,
    active                 BOOLEAN          NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMPTZ      NOT NULL DEFAULT now()
);

CREATE TABLE sla_validation_results (
    id           UUID         PRIMARY KEY,
    execution_id UUID         NOT NULL,
    policy_name  VARCHAR(100) NOT NULL,
    status       VARCHAR(10)  NOT NULL,
    evaluated_at TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_sla_validation_results_execution_id
    ON sla_validation_results (execution_id);

CREATE TABLE sla_rule_results (
    id                   UUID             PRIMARY KEY,
    validation_result_id UUID             NOT NULL
        REFERENCES sla_validation_results (id) ON DELETE CASCADE,
    rule_name            VARCHAR(100)     NOT NULL,
    metric               VARCHAR(100)     NOT NULL,
    comparator           VARCHAR(10)      NOT NULL,
    threshold            DOUBLE PRECISION NOT NULL,
    actual_value         DOUBLE PRECISION NOT NULL,
    status               VARCHAR(10)      NOT NULL
);

CREATE INDEX idx_sla_rule_results_validation_result_id
    ON sla_rule_results (validation_result_id);

-- Seed the default SLA policy from the module inputs: p95 < 300ms and error rate < 1%.
INSERT INTO sla_policies (id, name, max_p95_latency_ms, max_error_rate_percent, active)
VALUES (gen_random_uuid(), 'default', 300, 1.0, TRUE);
