-- Normalized product domain tables, workflow, audit, and transactional outbox.

CREATE TABLE product_price (
    id              UUID            NOT NULL,
    product_id      UUID            NOT NULL,
    unit_price      NUMERIC(19, 4)  NOT NULL,
    currency        CHAR(3)         NOT NULL DEFAULT 'INR',
    min_quantity    INTEGER         NOT NULL DEFAULT 1,
    discount_percent NUMERIC(5, 2),
    valid_from      TIMESTAMPTZ     NOT NULL,
    valid_to        TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_product_price PRIMARY KEY (id),
    CONSTRAINT fk_product_price_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT chk_product_price_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_product_price_min_quantity CHECK (min_quantity >= 1)
);

CREATE INDEX idx_product_price_product_id ON product_price (product_id);
CREATE INDEX idx_product_price_valid_from ON product_price (valid_from);

CREATE TABLE product_inventory (
    id                  UUID            NOT NULL,
    product_id          UUID            NOT NULL,
    quantity_available  INTEGER         NOT NULL DEFAULT 0,
    quantity_reserved   INTEGER         NOT NULL DEFAULT 0,
    reorder_level       INTEGER         NOT NULL DEFAULT 0,
    warehouse_code      VARCHAR(32),
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL,
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_product_inventory PRIMARY KEY (id),
    CONSTRAINT fk_product_inventory_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT uk_product_inventory_product UNIQUE (product_id),
    CONSTRAINT chk_product_inventory_available CHECK (quantity_available >= 0),
    CONSTRAINT chk_product_inventory_reserved CHECK (quantity_reserved >= 0)
);

CREATE TABLE product_attribute (
    id              UUID            NOT NULL,
    product_id      UUID            NOT NULL,
    attr_key        VARCHAR(128)    NOT NULL,
    attr_value      VARCHAR(2000)   NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_product_attribute PRIMARY KEY (id),
    CONSTRAINT fk_product_attribute_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT uk_product_attribute_key UNIQUE (product_id, attr_key)
);

CREATE INDEX idx_product_attribute_product_id ON product_attribute (product_id);

CREATE TABLE product_image (
    id              UUID            NOT NULL,
    product_id      UUID            NOT NULL,
    url             VARCHAR(2048)   NOT NULL,
    alt_text        VARCHAR(255),
    display_order   INTEGER         NOT NULL DEFAULT 0,
    primary_image   BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_product_image PRIMARY KEY (id),
    CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);

CREATE INDEX idx_product_image_product_id ON product_image (product_id);

CREATE TABLE product_document (
    id              UUID            NOT NULL,
    product_id      UUID            NOT NULL,
    document_type   VARCHAR(64)     NOT NULL,
    url             VARCHAR(2048)   NOT NULL,
    file_name       VARCHAR(255),
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_product_document PRIMARY KEY (id),
    CONSTRAINT fk_product_document_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);

CREATE INDEX idx_product_document_product_id ON product_document (product_id);

CREATE TABLE product_workflow (
    id              UUID            NOT NULL,
    product_id      UUID            NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    previous_status VARCHAR(32),
    message         VARCHAR(2000),
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_product_workflow PRIMARY KEY (id),
    CONSTRAINT fk_product_workflow_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT uk_product_workflow_product UNIQUE (product_id),
    CONSTRAINT chk_product_workflow_status CHECK (status IN (
        'INITIAL', 'VALIDATING', 'BUSINESS_VALIDATED', 'PERSISTED', 'OUTBOX_CREATED',
        'PUBLISHED', 'INDEXED', 'COMPLETED', 'FAILED', 'AMENDED', 'CANCELLED'
    ))
);

CREATE INDEX idx_product_workflow_status ON product_workflow (status);

CREATE TABLE product_audit (
    id              UUID            NOT NULL,
    product_id      UUID            NOT NULL,
    operation       VARCHAR(32)     NOT NULL,
    actor           VARCHAR(128),
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    before_state    JSONB,
    after_state     JSONB,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_product_audit PRIMARY KEY (id),
    CONSTRAINT fk_product_audit_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT chk_product_audit_operation CHECK (operation IN ('CREATE', 'UPDATE', 'PATCH', 'DELETE', 'SEARCH'))
);

CREATE INDEX idx_product_audit_product_id ON product_audit (product_id);
CREATE INDEX idx_product_audit_created_at ON product_audit (created_at);

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
