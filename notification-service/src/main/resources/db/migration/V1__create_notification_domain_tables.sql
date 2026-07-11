-- Notification service domain schema: templates, notifications, delivery, inbox, audit, outbox.

CREATE TABLE notification_template (
    id              UUID            NOT NULL,
    template_code   VARCHAR(64)     NOT NULL,
    channel         VARCHAR(32)     NOT NULL,
    subject         VARCHAR(500),
    body_template   TEXT            NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_notification_template PRIMARY KEY (id),
    CONSTRAINT uk_notification_template_code_channel UNIQUE (template_code, channel),
    CONSTRAINT chk_notification_template_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP', 'WEBHOOK'))
);

CREATE INDEX idx_notification_template_code ON notification_template (template_code);
CREATE INDEX idx_notification_template_active ON notification_template (active);

CREATE TABLE notification (
    id              UUID            NOT NULL,
    request_id      VARCHAR(64)     NOT NULL,
    correlation_id  VARCHAR(64),
    workflow_id     UUID,
    aggregate_type  VARCHAR(64),
    aggregate_id    UUID,
    notification_type VARCHAR(32)   NOT NULL,
    channel         VARCHAR(32)     NOT NULL,
    recipient_id    VARCHAR(128)    NOT NULL,
    recipient_address VARCHAR(512),
    subject         VARCHAR(500),
    body            TEXT            NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    previous_status VARCHAR(32),
    template_code   VARCHAR(64),
    metadata        JSONB,
    retry_count     INTEGER         NOT NULL DEFAULT 0,
    max_retries     INTEGER         NOT NULL DEFAULT 3,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    version         BIGINT          NOT NULL DEFAULT 0,
    sent_at         TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    CONSTRAINT pk_notification PRIMARY KEY (id),
    CONSTRAINT uk_notification_request_id UNIQUE (request_id),
    CONSTRAINT chk_notification_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP', 'WEBHOOK')),
    CONSTRAINT chk_notification_type CHECK (notification_type IN (
        'WORKFLOW_COMPLETED', 'WORKFLOW_FAILED', 'PRODUCT_CREATED', 'ORDER_UPDATE', 'SYSTEM_ALERT', 'CUSTOM'
    )),
    CONSTRAINT chk_notification_status CHECK (status IN (
        'PENDING', 'QUEUED', 'PROCESSING', 'SENT', 'DELIVERED', 'FAILED', 'RETRY', 'CANCELLED'
    ))
);

CREATE INDEX idx_notification_recipient_id ON notification (recipient_id);
CREATE INDEX idx_notification_status ON notification (status);
CREATE INDEX idx_notification_workflow_id ON notification (workflow_id);
CREATE INDEX idx_notification_correlation_id ON notification (correlation_id);
CREATE INDEX idx_notification_created_at ON notification (created_at);
CREATE INDEX idx_notification_active ON notification (active);

CREATE TABLE notification_delivery (
    id              UUID            NOT NULL,
    notification_id UUID            NOT NULL,
    channel         VARCHAR(32)     NOT NULL,
    attempt_number  INTEGER         NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    provider_response VARCHAR(2000),
    error_message   VARCHAR(2000),
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_notification_delivery PRIMARY KEY (id),
    CONSTRAINT fk_notification_delivery_notification FOREIGN KEY (notification_id) REFERENCES notification (id) ON DELETE CASCADE,
    CONSTRAINT chk_notification_delivery_status CHECK (status IN ('SUCCESS', 'FAILED', 'SKIPPED'))
);

CREATE INDEX idx_notification_delivery_notification_id ON notification_delivery (notification_id);
CREATE INDEX idx_notification_delivery_created_at ON notification_delivery (created_at);

CREATE TABLE notification_inbox (
    id              UUID            NOT NULL,
    notification_id UUID            NOT NULL,
    recipient_id    VARCHAR(128)    NOT NULL,
    subject         VARCHAR(500),
    body            TEXT            NOT NULL,
    read_flag       BOOLEAN         NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_notification_inbox PRIMARY KEY (id),
    CONSTRAINT fk_notification_inbox_notification FOREIGN KEY (notification_id) REFERENCES notification (id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_inbox_recipient_id ON notification_inbox (recipient_id);
CREATE INDEX idx_notification_inbox_read_flag ON notification_inbox (read_flag);

CREATE TABLE notification_audit (
    id              UUID            NOT NULL,
    notification_id UUID            NOT NULL,
    operation       VARCHAR(32)     NOT NULL,
    actor           VARCHAR(128),
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    before_status   VARCHAR(32),
    after_status    VARCHAR(32),
    before_state    JSONB,
    after_state     JSONB,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_notification_audit PRIMARY KEY (id),
    CONSTRAINT fk_notification_audit_notification FOREIGN KEY (notification_id) REFERENCES notification (id) ON DELETE CASCADE,
    CONSTRAINT chk_notification_audit_operation CHECK (operation IN (
        'CREATE', 'UPDATE', 'STATUS_CHANGE', 'DELETE', 'DISPATCH', 'RETRY', 'EVENT_RECEIVED'
    ))
);

CREATE INDEX idx_notification_audit_notification_id ON notification_audit (notification_id);
CREATE INDEX idx_notification_audit_created_at ON notification_audit (created_at);

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

-- Seed default templates
INSERT INTO notification_template (id, template_code, channel, subject, body_template, active, created_at, updated_at) VALUES
    (gen_random_uuid(), 'WORKFLOW_COMPLETED', 'IN_APP', 'Workflow Completed',
     'Workflow {{workflowId}} for {{aggregateType}} has completed successfully.', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'WORKFLOW_COMPLETED', 'EMAIL', 'Workflow Completed - {{aggregateType}}',
     'Dear user, workflow {{workflowId}} for {{aggregateType}} (ID: {{aggregateId}}) has completed.', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'WORKFLOW_FAILED', 'IN_APP', 'Workflow Failed',
     'Workflow {{workflowId}} for {{aggregateType}} has failed. Message: {{message}}', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'WORKFLOW_FAILED', 'EMAIL', 'Workflow Failed - {{aggregateType}}',
     'Dear user, workflow {{workflowId}} for {{aggregateType}} has failed. Details: {{message}}', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'PRODUCT_CREATED', 'IN_APP', 'Product Created',
     'Product {{aggregateId}} has been created successfully.', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SYSTEM_ALERT', 'IN_APP', 'System Alert',
     '{{message}}', TRUE, NOW(), NOW());
