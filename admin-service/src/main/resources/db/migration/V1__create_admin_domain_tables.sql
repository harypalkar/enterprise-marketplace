-- Admin service schema: platform settings, feature flags, configs, audit trail, outbox.

CREATE TABLE platform_setting (
    id              UUID            NOT NULL,
    setting_key     VARCHAR(128)    NOT NULL,
    setting_value   VARCHAR(2000)   NOT NULL,
    category        VARCHAR(64)     NOT NULL DEFAULT 'GENERAL',
    description     VARCHAR(512),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_platform_setting PRIMARY KEY (id),
    CONSTRAINT uk_platform_setting_key UNIQUE (setting_key)
);

CREATE INDEX idx_platform_setting_category ON platform_setting (category);
CREATE INDEX idx_platform_setting_active ON platform_setting (active);

CREATE TABLE feature_flag (
    id                  UUID            NOT NULL,
    flag_key            VARCHAR(128)    NOT NULL,
    enabled             BOOLEAN         NOT NULL DEFAULT FALSE,
    description         VARCHAR(512),
    rollout_percentage  INTEGER         NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_feature_flag PRIMARY KEY (id),
    CONSTRAINT uk_feature_flag_key UNIQUE (flag_key),
    CONSTRAINT chk_feature_flag_rollout CHECK (rollout_percentage >= 0 AND rollout_percentage <= 100)
);

CREATE INDEX idx_feature_flag_enabled ON feature_flag (enabled);

CREATE TABLE admin_config (
    id              UUID            NOT NULL,
    config_key      VARCHAR(128)    NOT NULL,
    config_value    JSONB           NOT NULL,
    scope           VARCHAR(64)     NOT NULL DEFAULT 'GLOBAL',
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_admin_config PRIMARY KEY (id),
    CONSTRAINT uk_admin_config_key UNIQUE (config_key)
);

CREATE INDEX idx_admin_config_scope ON admin_config (scope);
CREATE INDEX idx_admin_config_active ON admin_config (active);

CREATE TABLE admin_audit (
    id              UUID            NOT NULL,
    action          VARCHAR(64)     NOT NULL,
    entity_type     VARCHAR(64)     NOT NULL,
    entity_key      VARCHAR(128)    NOT NULL,
    entity_id       UUID,
    actor           VARCHAR(128)    NOT NULL,
    before_state    JSONB,
    after_state     JSONB,
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_admin_audit PRIMARY KEY (id),
    CONSTRAINT chk_admin_audit_action CHECK (action IN (
        'CREATE', 'UPDATE', 'DELETE', 'TOGGLE', 'PATCH'
    ))
);

CREATE INDEX idx_admin_audit_entity ON admin_audit (entity_type, entity_key);
CREATE INDEX idx_admin_audit_actor ON admin_audit (actor);
CREATE INDEX idx_admin_audit_created_at ON admin_audit (created_at);

CREATE TABLE platform_stat (
    id              UUID            NOT NULL,
    metric_key      VARCHAR(128)    NOT NULL,
    metric_value    BIGINT          NOT NULL DEFAULT 0,
    category        VARCHAR(64)     NOT NULL DEFAULT 'PLATFORM',
    description     VARCHAR(512),
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_platform_stat PRIMARY KEY (id),
    CONSTRAINT uk_platform_stat_key UNIQUE (metric_key)
);

CREATE INDEX idx_platform_stat_category ON platform_stat (category);

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

-- Seed platform settings
INSERT INTO platform_setting (id, setting_key, setting_value, category, description, active, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0001-000000000001', 'marketplace.name', 'Enterprise Marketplace', 'GENERAL', 'Platform display name', TRUE, NOW(), NOW()),
    ('00000000-0000-0000-0001-000000000002', 'marketplace.default_currency', 'USD', 'GENERAL', 'Default currency code (ISO 4217)', TRUE, NOW(), NOW()),
    ('00000000-0000-0000-0001-000000000003', 'feature.ai.enabled', 'true', 'FEATURES', 'Global AI feature toggle setting', TRUE, NOW(), NOW());

-- Seed feature flags
INSERT INTO feature_flag (id, flag_key, enabled, description, rollout_percentage, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0002-000000000001', 'feature.ai.enabled', TRUE, 'AI service integration feature flag', 100, NOW(), NOW());

-- Seed platform stats for dashboard
INSERT INTO platform_stat (id, metric_key, metric_value, category, description, updated_at)
VALUES
    ('00000000-0000-0000-0003-000000000001', 'subscriptions.total', 0, 'SUBSCRIPTIONS', 'Total active subscriptions', NOW()),
    ('00000000-0000-0000-0003-000000000002', 'reports.total', 0, 'REPORTS', 'Total generated reports', NOW()),
    ('00000000-0000-0000-0003-000000000003', 'users.total', 0, 'USERS', 'Total registered users', NOW()),
    ('00000000-0000-0000-0003-000000000004', 'products.total', 0, 'CATALOG', 'Total catalog products', NOW()),
    ('00000000-0000-0000-0003-000000000005', 'sellers.total', 0, 'USERS', 'Total seller accounts', NOW()),
    ('00000000-0000-0000-0003-000000000006', 'buyers.total', 0, 'USERS', 'Total buyer accounts', NOW());
