CREATE TABLE mobile_user (
    id                  UUID            NOT NULL,
    country_code        VARCHAR(8)      NOT NULL DEFAULT '+91',
    mobile_number       VARCHAR(20)     NOT NULL,
    user_type           VARCHAR(32),
    onboarding_step     VARCHAR(32)     NOT NULL DEFAULT 'OTP_VERIFIED',
    pin_hash            VARCHAR(255),
    pin_set_at          TIMESTAMP,
    failed_pin_attempts INT             NOT NULL DEFAULT 0,
    locked_until        TIMESTAMP,
    status              VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,
    created_by          VARCHAR(128),
    updated_by          VARCHAR(128),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_mobile_user PRIMARY KEY (id),
    CONSTRAINT uk_mobile_user_phone UNIQUE (country_code, mobile_number),
    CONSTRAINT chk_mobile_user_type CHECK (user_type IS NULL OR user_type IN ('INDIVIDUAL', 'BUSINESS')),
    CONSTRAINT chk_mobile_user_status CHECK (status IN ('ACTIVE', 'LOCKED', 'ARCHIVED')),
    CONSTRAINT chk_mobile_user_step CHECK (onboarding_step IN (
        'OTP_VERIFIED', 'TYPE_SET', 'DETAILS_SET', 'PIN_SET', 'COMPLETE'
    ))
);

CREATE TABLE user_profile (
    id              UUID            NOT NULL,
    user_id         UUID            NOT NULL,
    full_name       VARCHAR(255),
    legal_name      VARCHAR(255),
    email           VARCHAR(255),
    company_name    VARCHAR(255),
    website         VARCHAR(512),
    gst_number      VARCHAR(32),
    city            VARCHAR(100),
    country         VARCHAR(100),
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP       NOT NULL,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_user_profile PRIMARY KEY (id),
    CONSTRAINT uk_user_profile_user UNIQUE (user_id),
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES mobile_user (id)
);

CREATE INDEX idx_mobile_user_mobile ON mobile_user (mobile_number);
CREATE INDEX idx_mobile_user_status ON mobile_user (status);
CREATE INDEX idx_user_profile_email ON user_profile (email);
