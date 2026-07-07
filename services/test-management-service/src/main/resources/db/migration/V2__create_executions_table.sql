CREATE TABLE executions (
    id            UUID        PRIMARY KEY,
    test_id       UUID        NOT NULL,
    status        VARCHAR(20) NOT NULL,
    started_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at   TIMESTAMPTZ,
    error_message VARCHAR(2000),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_executions_test
        FOREIGN KEY (test_id) REFERENCES tests (id) ON DELETE CASCADE
);

CREATE INDEX idx_executions_test_id ON executions (test_id);
CREATE INDEX idx_executions_status  ON executions (status);
