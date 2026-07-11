-- Search service metadata schema: sync log, audit, outbox.

CREATE TABLE search_sync_log (
    id              UUID            NOT NULL,
    product_id      UUID            NOT NULL,
    operation       VARCHAR(32)     NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    payload         JSONB,
    error_message   VARCHAR(2000),
    created_at      TIMESTAMPTZ     NOT NULL,
    processed_at    TIMESTAMPTZ,
    CONSTRAINT pk_search_sync_log PRIMARY KEY (id),
    CONSTRAINT chk_search_sync_operation CHECK (operation IN ('INDEX', 'UPDATE', 'DELETE', 'REINDEX')),
    CONSTRAINT chk_search_sync_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'))
);

CREATE INDEX idx_search_sync_log_product_id ON search_sync_log (product_id);
CREATE INDEX idx_search_sync_log_status ON search_sync_log (status);
CREATE INDEX idx_search_sync_log_created_at ON search_sync_log (created_at);

CREATE TABLE search_audit (
    id              UUID            NOT NULL,
    product_id      UUID,
    operation       VARCHAR(32)     NOT NULL,
    actor           VARCHAR(128),
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    query_text      VARCHAR(1000),
    result_count    INTEGER,
    metadata        JSONB,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_search_audit PRIMARY KEY (id),
    CONSTRAINT chk_search_audit_operation CHECK (operation IN (
        'INDEX', 'UPDATE', 'DELETE', 'SEARCH', 'REINDEX', 'EVENT_RECEIVED'
    ))
);

CREATE INDEX idx_search_audit_product_id ON search_audit (product_id);
CREATE INDEX idx_search_audit_created_at ON search_audit (created_at);
CREATE INDEX idx_search_audit_correlation_id ON search_audit (correlation_id);

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
