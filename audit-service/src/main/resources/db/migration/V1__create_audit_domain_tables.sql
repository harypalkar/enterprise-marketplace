-- Central audit service schema: immutable audit records, event ingestion log, outbox.

CREATE TABLE audit_record (
    id              UUID            NOT NULL,
    event_key       VARCHAR(128)    NOT NULL,
    request_id      VARCHAR(64)     NOT NULL,
    correlation_id  VARCHAR(64),
    source_service  VARCHAR(64)     NOT NULL,
    aggregate_type  VARCHAR(64),
    aggregate_id    UUID,
    entity_type     VARCHAR(64),
    entity_id       UUID,
    operation       VARCHAR(32)     NOT NULL,
    actor           VARCHAR(128),
    before_state    JSONB,
    after_state     JSONB,
    metadata        JSONB,
    ip_address      VARCHAR(64),
    user_agent      VARCHAR(512),
    status          VARCHAR(32)     NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_audit_record PRIMARY KEY (id),
    CONSTRAINT uk_audit_record_event_key UNIQUE (event_key),
    CONSTRAINT chk_audit_record_operation CHECK (operation IN (
        'CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'READ', 'SEARCH',
        'LOGIN', 'LOGOUT', 'DISPATCH', 'EVENT_RECEIVED', 'EXPORT', 'ARCHIVE'
    )),
    CONSTRAINT chk_audit_record_status CHECK (status IN ('RECORDED', 'INDEXED', 'ARCHIVED', 'FAILED'))
);

CREATE INDEX idx_audit_record_request_id ON audit_record (request_id);
CREATE INDEX idx_audit_record_correlation_id ON audit_record (correlation_id);
CREATE INDEX idx_audit_record_source_service ON audit_record (source_service);
CREATE INDEX idx_audit_record_aggregate ON audit_record (aggregate_type, aggregate_id);
CREATE INDEX idx_audit_record_actor ON audit_record (actor);
CREATE INDEX idx_audit_record_operation ON audit_record (operation);
CREATE INDEX idx_audit_record_created_at ON audit_record (created_at);
CREATE INDEX idx_audit_record_active ON audit_record (active);

CREATE TABLE audit_event_log (
    id              UUID            NOT NULL,
    audit_record_id UUID,
    event_source    VARCHAR(64)     NOT NULL,
    event_type      VARCHAR(128)    NOT NULL,
    payload         JSONB           NOT NULL,
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    processed       BOOLEAN         NOT NULL DEFAULT FALSE,
    error_message   VARCHAR(2000),
    created_at      TIMESTAMPTZ     NOT NULL,
    processed_at    TIMESTAMPTZ,
    CONSTRAINT pk_audit_event_log PRIMARY KEY (id),
    CONSTRAINT fk_audit_event_log_record FOREIGN KEY (audit_record_id) REFERENCES audit_record (id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_event_log_request_id ON audit_event_log (request_id);
CREATE INDEX idx_audit_event_log_correlation_id ON audit_event_log (correlation_id);
CREATE INDEX idx_audit_event_log_processed ON audit_event_log (processed);
CREATE INDEX idx_audit_event_log_created_at ON audit_event_log (created_at);

CREATE TABLE audit_timeline (
    id              UUID            NOT NULL,
    correlation_id  VARCHAR(64)     NOT NULL,
    audit_record_id UUID            NOT NULL,
    sequence_number BIGINT          NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_audit_timeline PRIMARY KEY (id),
    CONSTRAINT fk_audit_timeline_record FOREIGN KEY (audit_record_id) REFERENCES audit_record (id) ON DELETE CASCADE,
    CONSTRAINT uk_audit_timeline_correlation_seq UNIQUE (correlation_id, sequence_number)
);

CREATE INDEX idx_audit_timeline_correlation_id ON audit_timeline (correlation_id);

CREATE TABLE outbox_event (
    id              UUID            NOT NULL,
    aggregate_type  VARCHAR(64)     NOT NULL,
    aggregate_id    UUID            NOT NULL,
    event_type      VARCHAR(128)    NOT NULL,
    topic           VARCHAR(128)    NOT NULL,
    payload         JSONB           NOT NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'PENDING',
    retry_count     INTEGER         NOT NULL DEFAULT 0,
    max_retries     INTEGER         NOT NULL DEFAULT 5,
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    last_error      VARCHAR(2000),
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    published_at    TIMESTAMPTZ,
    CONSTRAINT pk_outbox_event PRIMARY KEY (id),
    CONSTRAINT chk_outbox_event_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED', 'DEAD_LETTER'))
);

CREATE INDEX idx_outbox_event_status ON outbox_event (status);
CREATE INDEX idx_outbox_event_created_at ON outbox_event (created_at);
CREATE INDEX idx_outbox_event_aggregate ON outbox_event (aggregate_type, aggregate_id);
