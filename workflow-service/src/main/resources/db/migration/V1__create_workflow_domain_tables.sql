-- Workflow service domain schema: workflow lifecycle, history, transitions, events, audit, outbox.

CREATE TABLE workflow (
    id              UUID            NOT NULL,
    request_id      VARCHAR(64)     NOT NULL,
    correlation_id  VARCHAR(64),
    aggregate_type  VARCHAR(64)     NOT NULL,
    aggregate_id    UUID            NOT NULL,
    operation_type  VARCHAR(32)     NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    previous_status VARCHAR(32),
    tenant_id       VARCHAR(64),
    source_system   VARCHAR(64),
    initiated_by    VARCHAR(128),
    message         VARCHAR(2000),
    metadata        JSONB,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_workflow PRIMARY KEY (id),
    CONSTRAINT uk_workflow_request_id UNIQUE (request_id),
    CONSTRAINT chk_workflow_status CHECK (status IN (
        'INITIAL', 'RECEIVED', 'TECHNICAL_VALIDATION', 'BUSINESS_VALIDATION', 'REDIS_VALIDATION',
        'DATABASE_SAVED', 'OUTBOX_CREATED', 'EVENT_PUBLISHED', 'SEARCH_UPDATED', 'NOTIFICATION_SENT',
        'AI_COMPLETED', 'COMPLETED', 'FAILED', 'RETRY', 'CANCELLED', 'AMENDED', 'ROLLBACK'
    )),
    CONSTRAINT chk_workflow_operation_type CHECK (operation_type IN (
        'CREATE', 'UPDATE', 'PATCH', 'DELETE', 'SEARCH', 'IMPORT', 'EXPORT'
    ))
);

CREATE INDEX idx_workflow_aggregate ON workflow (aggregate_type, aggregate_id);
CREATE INDEX idx_workflow_status ON workflow (status);
CREATE INDEX idx_workflow_correlation_id ON workflow (correlation_id);
CREATE INDEX idx_workflow_created_at ON workflow (created_at);
CREATE INDEX idx_workflow_active ON workflow (active);

CREATE TABLE workflow_history (
    id              UUID            NOT NULL,
    workflow_id     UUID            NOT NULL,
    from_status     VARCHAR(32),
    to_status       VARCHAR(32)     NOT NULL,
    transition_reason VARCHAR(2000),
    transitioned_by VARCHAR(128),
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_workflow_history PRIMARY KEY (id),
    CONSTRAINT fk_workflow_history_workflow FOREIGN KEY (workflow_id) REFERENCES workflow (id) ON DELETE CASCADE
);

CREATE INDEX idx_workflow_history_workflow_id ON workflow_history (workflow_id);
CREATE INDEX idx_workflow_history_created_at ON workflow_history (created_at);

CREATE TABLE workflow_transition (
    id              UUID            NOT NULL,
    from_status     VARCHAR(32)     NOT NULL,
    to_status       VARCHAR(32)     NOT NULL,
    description     VARCHAR(500),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_workflow_transition PRIMARY KEY (id),
    CONSTRAINT uk_workflow_transition_from_to UNIQUE (from_status, to_status)
);

CREATE INDEX idx_workflow_transition_from_status ON workflow_transition (from_status);
CREATE INDEX idx_workflow_transition_active ON workflow_transition (active);

CREATE TABLE workflow_event (
    id              UUID            NOT NULL,
    workflow_id     UUID            NOT NULL,
    event_type      VARCHAR(128)    NOT NULL,
    event_source    VARCHAR(64)     NOT NULL,
    payload         JSONB           NOT NULL,
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_workflow_event PRIMARY KEY (id),
    CONSTRAINT fk_workflow_event_workflow FOREIGN KEY (workflow_id) REFERENCES workflow (id) ON DELETE CASCADE
);

CREATE INDEX idx_workflow_event_workflow_id ON workflow_event (workflow_id);
CREATE INDEX idx_workflow_event_type ON workflow_event (event_type);
CREATE INDEX idx_workflow_event_created_at ON workflow_event (created_at);

CREATE TABLE workflow_audit (
    id              UUID            NOT NULL,
    workflow_id     UUID            NOT NULL,
    operation       VARCHAR(32)     NOT NULL,
    actor           VARCHAR(128),
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    before_status   VARCHAR(32),
    after_status    VARCHAR(32),
    before_state    JSONB,
    after_state     JSONB,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_workflow_audit PRIMARY KEY (id),
    CONSTRAINT fk_workflow_audit_workflow FOREIGN KEY (workflow_id) REFERENCES workflow (id) ON DELETE CASCADE,
    CONSTRAINT chk_workflow_audit_operation CHECK (operation IN (
        'CREATE', 'UPDATE', 'STATUS_CHANGE', 'DELETE', 'SEARCH', 'EVENT_RECEIVED'
    ))
);

CREATE INDEX idx_workflow_audit_workflow_id ON workflow_audit (workflow_id);
CREATE INDEX idx_workflow_audit_created_at ON workflow_audit (created_at);

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

-- Seed allowed workflow transitions
INSERT INTO workflow_transition (id, from_status, to_status, description, active, created_at, updated_at) VALUES
    (gen_random_uuid(), 'INITIAL', 'RECEIVED', 'Workflow received for processing', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'RECEIVED', 'TECHNICAL_VALIDATION', 'Start technical validation', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'TECHNICAL_VALIDATION', 'BUSINESS_VALIDATION', 'Technical validation passed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'TECHNICAL_VALIDATION', 'FAILED', 'Technical validation failed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'BUSINESS_VALIDATION', 'REDIS_VALIDATION', 'Business validation passed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'BUSINESS_VALIDATION', 'FAILED', 'Business validation failed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'REDIS_VALIDATION', 'DATABASE_SAVED', 'Redis reference validation passed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'REDIS_VALIDATION', 'FAILED', 'Redis validation failed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'DATABASE_SAVED', 'OUTBOX_CREATED', 'Entity persisted to database', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'DATABASE_SAVED', 'FAILED', 'Database save failed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'OUTBOX_CREATED', 'EVENT_PUBLISHED', 'Outbox event created', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'OUTBOX_CREATED', 'FAILED', 'Outbox creation failed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'EVENT_PUBLISHED', 'SEARCH_UPDATED', 'Kafka event published', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'EVENT_PUBLISHED', 'FAILED', 'Event publish failed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SEARCH_UPDATED', 'NOTIFICATION_SENT', 'Search index updated', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SEARCH_UPDATED', 'FAILED', 'Search update failed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'NOTIFICATION_SENT', 'AI_COMPLETED', 'Notification dispatched', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'NOTIFICATION_SENT', 'COMPLETED', 'Workflow completed without AI step', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'NOTIFICATION_SENT', 'FAILED', 'Notification failed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'AI_COMPLETED', 'COMPLETED', 'AI processing completed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'AI_COMPLETED', 'FAILED', 'AI processing failed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'FAILED', 'RETRY', 'Retry failed workflow', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'RETRY', 'RECEIVED', 'Retry re-enters processing', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'INITIAL', 'CANCELLED', 'Cancel before processing', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'RECEIVED', 'CANCELLED', 'Cancel during intake', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'TECHNICAL_VALIDATION', 'CANCELLED', 'Cancel during technical validation', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'BUSINESS_VALIDATION', 'CANCELLED', 'Cancel during business validation', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'COMPLETED', 'AMENDED', 'Amend completed workflow', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'AMENDED', 'RECEIVED', 'Amended workflow re-processed', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'FAILED', 'ROLLBACK', 'Rollback failed workflow', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'ROLLBACK', 'CANCELLED', 'Rollback leads to cancellation', TRUE, NOW(), NOW());
