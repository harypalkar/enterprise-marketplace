-- AI service domain schema: chat sessions, generation logs, prompts, audit, outbox.

CREATE TABLE ai_chat_session (
    id              UUID            NOT NULL,
    session_key     VARCHAR(128)    NOT NULL,
    user_id         VARCHAR(128)    NOT NULL,
    user_role       VARCHAR(32)     NOT NULL,
    title           VARCHAR(500),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_ai_chat_session PRIMARY KEY (id),
    CONSTRAINT uk_ai_chat_session_key UNIQUE (session_key)
);

CREATE INDEX idx_ai_chat_session_user_id ON ai_chat_session (user_id);
CREATE INDEX idx_ai_chat_session_active ON ai_chat_session (active);

CREATE TABLE ai_chat_message (
    id              UUID            NOT NULL,
    session_id      UUID            NOT NULL,
    role            VARCHAR(32)     NOT NULL,
    content         TEXT            NOT NULL,
    model           VARCHAR(64),
    tokens_used     INTEGER,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_ai_chat_message PRIMARY KEY (id),
    CONSTRAINT fk_ai_chat_message_session FOREIGN KEY (session_id) REFERENCES ai_chat_session (id) ON DELETE CASCADE,
    CONSTRAINT chk_ai_chat_message_role CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM'))
);

CREATE INDEX idx_ai_chat_message_session_id ON ai_chat_message (session_id);

CREATE TABLE ai_prompt_template (
    id              UUID            NOT NULL,
    template_code   VARCHAR(64)     NOT NULL,
    use_case        VARCHAR(64)     NOT NULL,
    model           VARCHAR(64),
    system_prompt   TEXT,
    user_prompt     TEXT            NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_ai_prompt_template PRIMARY KEY (id),
    CONSTRAINT uk_ai_prompt_template_code UNIQUE (template_code),
    CONSTRAINT chk_ai_prompt_use_case CHECK (use_case IN (
        'PRODUCT_DESCRIPTION', 'BUYER_CHAT', 'SEARCH_INTERPRET', 'RECOMMENDATION', 'RFQ_DRAFT'
    ))
);

CREATE TABLE ai_generation_log (
    id              UUID            NOT NULL,
    request_id      VARCHAR(64)     NOT NULL,
    correlation_id  VARCHAR(64),
    use_case        VARCHAR(64)     NOT NULL,
    model           VARCHAR(64)     NOT NULL,
    user_id         VARCHAR(128),
    aggregate_type  VARCHAR(64),
    aggregate_id    UUID,
    prompt          TEXT            NOT NULL,
    response        TEXT,
    status          VARCHAR(32)     NOT NULL,
    latency_ms      BIGINT,
    error_message   VARCHAR(2000),
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_ai_generation_log PRIMARY KEY (id),
    CONSTRAINT uk_ai_generation_request_id UNIQUE (request_id),
    CONSTRAINT chk_ai_generation_status CHECK (status IN ('SUCCESS', 'FAILED', 'TIMEOUT'))
);

CREATE INDEX idx_ai_generation_log_user_id ON ai_generation_log (user_id);
CREATE INDEX idx_ai_generation_log_created_at ON ai_generation_log (created_at);

CREATE TABLE ai_audit (
    id              UUID            NOT NULL,
    operation       VARCHAR(32)     NOT NULL,
    actor           VARCHAR(128),
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    metadata        JSONB,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_ai_audit PRIMARY KEY (id),
    CONSTRAINT chk_ai_audit_operation CHECK (operation IN (
        'CHAT', 'GENERATE', 'INTERPRET', 'RECOMMEND', 'EVENT_RECEIVED', 'FEATURE_TOGGLE'
    ))
);

CREATE INDEX idx_ai_audit_created_at ON ai_audit (created_at);

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

INSERT INTO ai_prompt_template (id, template_code, use_case, model, system_prompt, user_prompt, active, created_at, updated_at) VALUES
    (gen_random_uuid(), 'PRODUCT_DESCRIPTION', 'PRODUCT_DESCRIPTION', 'llama3.2',
     'You are a B2B marketplace copywriter for an IndiaMART-like platform. Write professional product descriptions with MOQ, specs, and GST-ready tone.',
     'Generate a product description for:\nName: {{name}}\nCategory: {{categoryId}}\nSKU: {{sku}}\nAttributes: {{attributes}}\nKeep it under 200 words.',
     TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'BUYER_CHAT', 'BUYER_CHAT', 'llama3.2',
     'You are a helpful B2B marketplace assistant. Help buyers find products. Do not commit to prices. Suggest contacting sellers for quotes.',
     '{{message}}', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SEARCH_INTERPRET', 'SEARCH_INTERPRET', 'llama3.2',
     'Convert natural language search queries into JSON with fields: q, categoryId, minPrice, maxPrice, status. Return JSON only.',
     'Query: {{query}}', TRUE, NOW(), NOW());
