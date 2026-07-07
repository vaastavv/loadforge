CREATE TABLE tests (
    id               UUID          PRIMARY KEY,
    name             VARCHAR(255)  NOT NULL,
    description      VARCHAR(1000),
    target_url       VARCHAR(2048) NOT NULL,
    http_method      VARCHAR(10)   NOT NULL,
    virtual_users    INTEGER       NOT NULL,
    duration_seconds INTEGER       NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_tests_virtual_users    CHECK (virtual_users > 0),
    CONSTRAINT chk_tests_duration_seconds CHECK (duration_seconds > 0)
);

CREATE INDEX idx_tests_name ON tests (name);
