-- Subscription service domain schema: plans, subscriptions, billing, audit, outbox.

CREATE TABLE subscription_plan (
    id              UUID            NOT NULL,
    plan_code       VARCHAR(32)     NOT NULL,
    name            VARCHAR(128)    NOT NULL,
    tier            VARCHAR(32)     NOT NULL,
    price           DECIMAL(12, 2)  NOT NULL,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'USD',
    billing_cycle   VARCHAR(32)     NOT NULL,
    features        JSONB,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_subscription_plan PRIMARY KEY (id),
    CONSTRAINT uk_subscription_plan_code UNIQUE (plan_code),
    CONSTRAINT chk_subscription_plan_tier CHECK (tier IN ('FREE', 'BASIC', 'PREMIUM')),
    CONSTRAINT chk_subscription_plan_billing_cycle CHECK (billing_cycle IN ('NONE', 'MONTHLY', 'YEARLY'))
);

CREATE INDEX idx_subscription_plan_tier ON subscription_plan (tier);
CREATE INDEX idx_subscription_plan_active ON subscription_plan (active);

CREATE TABLE subscription (
    id              UUID            NOT NULL,
    request_id      VARCHAR(64)     NOT NULL,
    seller_id       UUID            NOT NULL,
    buyer_id        UUID            NOT NULL,
    plan_id         UUID            NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    start_date      DATE,
    end_date        DATE,
    auto_renew      BOOLEAN         NOT NULL DEFAULT FALSE,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_subscription PRIMARY KEY (id),
    CONSTRAINT uk_subscription_request_id UNIQUE (request_id),
    CONSTRAINT fk_subscription_plan FOREIGN KEY (plan_id) REFERENCES subscription_plan (id),
    CONSTRAINT chk_subscription_status CHECK (status IN (
        'PENDING', 'ACTIVE', 'CANCELLED', 'EXPIRED', 'SUSPENDED'
    ))
);

CREATE INDEX idx_subscription_seller_id ON subscription (seller_id);
CREATE INDEX idx_subscription_buyer_id ON subscription (buyer_id);
CREATE INDEX idx_subscription_plan_id ON subscription (plan_id);
CREATE INDEX idx_subscription_status ON subscription (status);
CREATE INDEX idx_subscription_active ON subscription (active);
CREATE INDEX idx_subscription_created_at ON subscription (created_at);

CREATE TABLE subscription_billing (
    id              UUID            NOT NULL,
    subscription_id UUID            NOT NULL,
    amount          DECIMAL(12, 2)  NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    billing_date    DATE            NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_subscription_billing PRIMARY KEY (id),
    CONSTRAINT fk_subscription_billing_subscription FOREIGN KEY (subscription_id) REFERENCES subscription (id) ON DELETE CASCADE,
    CONSTRAINT chk_subscription_billing_status CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED'))
);

CREATE INDEX idx_subscription_billing_subscription_id ON subscription_billing (subscription_id);
CREATE INDEX idx_subscription_billing_billing_date ON subscription_billing (billing_date);
CREATE INDEX idx_subscription_billing_status ON subscription_billing (status);

CREATE TABLE subscription_audit (
    id              UUID            NOT NULL,
    subscription_id UUID            NOT NULL,
    operation       VARCHAR(32)     NOT NULL,
    actor           VARCHAR(128),
    correlation_id  VARCHAR(64),
    request_id      VARCHAR(64),
    before_status   VARCHAR(32),
    after_status    VARCHAR(32),
    before_state    JSONB,
    after_state     JSONB,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_subscription_audit PRIMARY KEY (id),
    CONSTRAINT fk_subscription_audit_subscription FOREIGN KEY (subscription_id) REFERENCES subscription (id) ON DELETE CASCADE,
    CONSTRAINT chk_subscription_audit_operation CHECK (operation IN (
        'CREATE', 'UPDATE', 'STATUS_CHANGE', 'DELETE', 'RENEW', 'CANCEL', 'EVENT_RECEIVED'
    ))
);

CREATE INDEX idx_subscription_audit_subscription_id ON subscription_audit (subscription_id);
CREATE INDEX idx_subscription_audit_created_at ON subscription_audit (created_at);

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

INSERT INTO subscription_plan (id, plan_code, name, tier, price, currency, billing_cycle, features, active, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111001', 'FREE', 'Free Plan', 'FREE', 0.00, 'USD', 'NONE',
     '{"maxListings": 5, "support": "community", "analytics": false}'::jsonb, TRUE, NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111002', 'BASIC', 'Basic Plan', 'BASIC', 9.99, 'USD', 'MONTHLY',
     '{"maxListings": 50, "support": "email", "analytics": true}'::jsonb, TRUE, NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111003', 'PREMIUM', 'Premium Plan', 'PREMIUM', 29.99, 'USD', 'MONTHLY',
     '{"maxListings": -1, "support": "priority", "analytics": true, "apiAccess": true}'::jsonb, TRUE, NOW(), NOW());
