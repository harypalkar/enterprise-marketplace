-- Report service schema: definitions, async jobs, results, domain audit, outbox.

CREATE TABLE report_definition (
    id                  UUID            NOT NULL,
    report_code         VARCHAR(64)     NOT NULL,
    name                VARCHAR(256)    NOT NULL,
    report_type         VARCHAR(32)     NOT NULL,
    query_template      TEXT,
    parameters_schema   JSONB,
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_report_definition PRIMARY KEY (id),
    CONSTRAINT uk_report_definition_code UNIQUE (report_code),
    CONSTRAINT chk_report_definition_type CHECK (report_type IN ('ANALYTICS', 'OPERATIONAL', 'SNAPSHOT'))
);

CREATE INDEX idx_report_definition_active ON report_definition (active);
CREATE INDEX idx_report_definition_type ON report_definition (report_type);

CREATE TABLE report_job (
    id              UUID            NOT NULL,
    request_id      VARCHAR(64)     NOT NULL,
    report_code     VARCHAR(64)     NOT NULL,
    requested_by    VARCHAR(128)    NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    parameters      JSONB,
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    error_message   VARCHAR(2000),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_report_job PRIMARY KEY (id),
    CONSTRAINT uk_report_job_request_id UNIQUE (request_id),
    CONSTRAINT fk_report_job_definition FOREIGN KEY (report_code) REFERENCES report_definition (report_code),
    CONSTRAINT chk_report_job_status CHECK (status IN (
        'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED'
    ))
);

CREATE INDEX idx_report_job_report_code ON report_job (report_code);
CREATE INDEX idx_report_job_status ON report_job (status);
CREATE INDEX idx_report_job_requested_by ON report_job (requested_by);
CREATE INDEX idx_report_job_created_at ON report_job (created_at);
CREATE INDEX idx_report_job_active ON report_job (active);

CREATE TABLE report_result (
    id              UUID            NOT NULL,
    job_id          UUID            NOT NULL,
    result_data     JSONB           NOT NULL,
    row_count       INTEGER         NOT NULL DEFAULT 0,
    file_url        VARCHAR(1024),
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_report_result PRIMARY KEY (id),
    CONSTRAINT fk_report_result_job FOREIGN KEY (job_id) REFERENCES report_job (id) ON DELETE CASCADE,
    CONSTRAINT uk_report_result_job UNIQUE (job_id)
);

CREATE INDEX idx_report_result_created_at ON report_result (created_at);

CREATE TABLE report_audit (
    id              UUID            NOT NULL,
    job_id          UUID,
    operation       VARCHAR(32)     NOT NULL,
    actor           VARCHAR(128),
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    before_status   VARCHAR(32),
    after_status    VARCHAR(32),
    before_state    JSONB,
    after_state     JSONB,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_report_audit PRIMARY KEY (id),
    CONSTRAINT fk_report_audit_job FOREIGN KEY (job_id) REFERENCES report_job (id) ON DELETE SET NULL,
    CONSTRAINT chk_report_audit_operation CHECK (operation IN (
        'CREATE', 'UPDATE', 'STATUS_CHANGE', 'DELETE', 'SEARCH', 'EVENT_RECEIVED', 'GENERATE', 'CANCEL'
    ))
);

CREATE INDEX idx_report_audit_job_id ON report_audit (job_id);
CREATE INDEX idx_report_audit_created_at ON report_audit (created_at);

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

INSERT INTO report_definition (id, report_code, name, report_type, query_template, parameters_schema, active, created_at, updated_at)
VALUES
    (
        'a1000000-0000-4000-8000-000000000001',
        'SALES_SUMMARY',
        'Sales Summary Report',
        'ANALYTICS',
        'SELECT date_trunc(''day'', order_date) AS period, SUM(total_amount) AS revenue, COUNT(*) AS order_count FROM orders WHERE order_date BETWEEN :fromDate AND :toDate GROUP BY 1 ORDER BY 1',
        '{"type":"object","required":["fromDate","toDate"],"properties":{"fromDate":{"type":"string","format":"date"},"toDate":{"type":"string","format":"date"},"sellerId":{"type":"string"}}}',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'a1000000-0000-4000-8000-000000000002',
        'WORKFLOW_STATUS',
        'Workflow Status Report',
        'OPERATIONAL',
        'SELECT status, COUNT(*) AS workflow_count FROM workflow WHERE updated_at >= :asOfDate GROUP BY status ORDER BY status',
        '{"type":"object","required":["asOfDate"],"properties":{"asOfDate":{"type":"string","format":"date-time"},"aggregateType":{"type":"string"}}}',
        TRUE,
        NOW(),
        NOW()
    ),
    (
        'a1000000-0000-4000-8000-000000000003',
        'INVENTORY_SNAPSHOT',
        'Inventory Snapshot Report',
        'SNAPSHOT',
        'SELECT product_id, sku, quantity_on_hand, reorder_level FROM inventory WHERE snapshot_date = :snapshotDate',
        '{"type":"object","required":["snapshotDate"],"properties":{"snapshotDate":{"type":"string","format":"date"},"categoryId":{"type":"string"}}}',
        TRUE,
        NOW(),
        NOW()
    );
