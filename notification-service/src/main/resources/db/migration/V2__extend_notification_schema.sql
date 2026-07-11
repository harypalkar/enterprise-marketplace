-- Extend notification domain: history, channel config, retry tracking, template content types, status alignment.

ALTER TABLE notification_template
    ADD COLUMN IF NOT EXISTS content_type VARCHAR(32) NOT NULL DEFAULT 'PLAIN_TEXT';

ALTER TABLE notification_template
    ADD CONSTRAINT chk_notification_template_content_type
        CHECK (content_type IN ('HTML', 'PLAIN_TEXT', 'SMS', 'PUSH', 'WEBHOOK'));

ALTER TABLE notification DROP CONSTRAINT IF EXISTS chk_notification_status;
UPDATE notification SET status = 'CREATED' WHERE status = 'PENDING';
UPDATE notification SET status = 'RETRYING' WHERE status = 'RETRY';
ALTER TABLE notification
    ADD CONSTRAINT chk_notification_status CHECK (status IN (
        'CREATED', 'QUEUED', 'PROCESSING', 'SENT', 'DELIVERED', 'FAILED', 'RETRYING', 'CANCELLED', 'EXPIRED'
    ));

ALTER TABLE notification DROP CONSTRAINT IF EXISTS chk_notification_type;
ALTER TABLE notification
    ADD CONSTRAINT chk_notification_type CHECK (notification_type IN (
        'WORKFLOW_COMPLETED', 'WORKFLOW_FAILED', 'PRODUCT_CREATED', 'PRODUCT_UPDATED',
        'SELLER_APPROVED', 'BUYER_REGISTERED', 'INVENTORY_LOW', 'SUBSCRIPTION_EXPIRED',
        'ORDER_UPDATE', 'SYSTEM_ALERT', 'CUSTOM'
    ));

ALTER TABLE notification
    ADD COLUMN IF NOT EXISTS expires_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_notification_expires_at ON notification (expires_at);

CREATE TABLE notification_history (
    id              UUID            NOT NULL,
    notification_id UUID            NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    channel         VARCHAR(32)     NOT NULL,
    event_type      VARCHAR(64)     NOT NULL,
    message         VARCHAR(2000),
    metadata        JSONB,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_notification_history PRIMARY KEY (id),
    CONSTRAINT fk_notification_history_notification FOREIGN KEY (notification_id) REFERENCES notification (id) ON DELETE CASCADE,
    CONSTRAINT chk_notification_history_status CHECK (status IN (
        'CREATED', 'QUEUED', 'PROCESSING', 'SENT', 'DELIVERED', 'FAILED', 'RETRYING', 'CANCELLED', 'EXPIRED'
    ))
);

CREATE INDEX idx_notification_history_notification_id ON notification_history (notification_id);
CREATE INDEX idx_notification_history_created_at ON notification_history (created_at);

CREATE TABLE notification_channel (
    id              UUID            NOT NULL,
    channel         VARCHAR(32)     NOT NULL,
    provider        VARCHAR(64)     NOT NULL,
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,
    config          JSONB           NOT NULL DEFAULT '{}',
    rate_limit_per_hour INTEGER     NOT NULL DEFAULT 100,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_notification_channel PRIMARY KEY (id),
    CONSTRAINT uk_notification_channel UNIQUE (channel),
    CONSTRAINT chk_notification_channel_type CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP', 'WEBHOOK'))
);

CREATE INDEX idx_notification_channel_enabled ON notification_channel (enabled);

CREATE TABLE notification_retry (
    id              UUID            NOT NULL,
    notification_id UUID            NOT NULL,
    attempt_number  INTEGER         NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    error_message   VARCHAR(2000),
    scheduled_at    TIMESTAMPTZ     NOT NULL,
    processed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL,
    CONSTRAINT pk_notification_retry PRIMARY KEY (id),
    CONSTRAINT fk_notification_retry_notification FOREIGN KEY (notification_id) REFERENCES notification (id) ON DELETE CASCADE,
    CONSTRAINT chk_notification_retry_status CHECK (status IN ('SCHEDULED', 'PROCESSING', 'COMPLETED', 'FAILED', 'EXHAUSTED'))
);

CREATE INDEX idx_notification_retry_notification_id ON notification_retry (notification_id);
CREATE INDEX idx_notification_retry_status ON notification_retry (status);
CREATE INDEX idx_notification_retry_scheduled_at ON notification_retry (scheduled_at);

INSERT INTO notification_channel (id, channel, provider, enabled, config, rate_limit_per_hour, created_at, updated_at) VALUES
    (gen_random_uuid(), 'EMAIL', 'SMTP', TRUE, '{"provider":"SMTP","sesReady":true}', 200, NOW(), NOW()),
    (gen_random_uuid(), 'SMS', 'TWILIO', TRUE, '{"provider":"TWILIO"}', 50, NOW(), NOW()),
    (gen_random_uuid(), 'PUSH', 'FCM', TRUE, '{"provider":"FCM"}', 500, NOW(), NOW()),
    (gen_random_uuid(), 'WEBHOOK', 'REST', TRUE, '{"provider":"REST"}', 300, NOW(), NOW()),
    (gen_random_uuid(), 'IN_APP', 'DATABASE', TRUE, '{"provider":"DATABASE"}', 1000, NOW(), NOW())
ON CONFLICT (channel) DO NOTHING;

UPDATE notification_template SET content_type = 'PLAIN_TEXT' WHERE channel IN ('SMS', 'IN_APP');
UPDATE notification_template SET content_type = 'HTML' WHERE channel = 'EMAIL';

INSERT INTO notification_template (id, template_code, channel, subject, body_template, content_type, active, created_at, updated_at) VALUES
    (gen_random_uuid(), 'PRODUCT_CREATED', 'EMAIL', 'Product Created - {{aggregateId}}',
     '<p>Product <strong>{{aggregateId}}</strong> has been created successfully.</p>', 'HTML', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'PRODUCT_UPDATED', 'IN_APP', 'Product Updated',
     'Product {{aggregateId}} has been updated.', 'PLAIN_TEXT', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'PRODUCT_UPDATED', 'EMAIL', 'Product Updated - {{aggregateId}}',
     '<p>Product {{aggregateId}} was updated.</p>', 'HTML', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SELLER_APPROVED', 'IN_APP', 'Seller Approved',
     'Seller account {{aggregateId}} has been approved.', 'PLAIN_TEXT', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SELLER_APPROVED', 'EMAIL', 'Seller Account Approved',
     '<p>Your seller account has been approved. Welcome to Enterprise Marketplace.</p>', 'HTML', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'BUYER_REGISTERED', 'IN_APP', 'Welcome',
     'Welcome {{recipientId}}! Your buyer account is ready.', 'PLAIN_TEXT', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'INVENTORY_LOW', 'IN_APP', 'Low Inventory Alert',
     'Inventory for product {{aggregateId}} is low. Current level: {{quantity}}', 'PLAIN_TEXT', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'INVENTORY_LOW', 'EMAIL', 'Low Inventory Alert',
     '<p>Product {{aggregateId}} inventory is low ({{quantity}} remaining).</p>', 'HTML', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SUBSCRIPTION_EXPIRED', 'IN_APP', 'Subscription Expired',
     'Your subscription {{aggregateId}} has expired.', 'PLAIN_TEXT', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SUBSCRIPTION_EXPIRED', 'EMAIL', 'Subscription Expired',
     '<p>Your subscription has expired. Please renew to continue service.</p>', 'HTML', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'WORKFLOW_COMPLETED', 'SMS', NULL,
     'Workflow {{workflowId}} completed for {{aggregateType}}.', 'SMS', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'WORKFLOW_FAILED', 'PUSH', 'Workflow Failed',
     '{"title":"Workflow Failed","body":"Workflow {{workflowId}} failed: {{message}}"}', 'PUSH', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SYSTEM_ALERT', 'WEBHOOK', NULL,
     '{"event":"system-alert","message":"{{message}}","severity":"{{severity}}"}', 'WEBHOOK', TRUE, NOW(), NOW())
ON CONFLICT (template_code, channel) DO NOTHING;
