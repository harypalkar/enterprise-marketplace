CREATE TABLE buyer (
    id              UUID            NOT NULL,
    company_name    VARCHAR(255)    NOT NULL,
    contact_person  VARCHAR(128)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    phone           VARCHAR(32)     NOT NULL,
    city            VARCHAR(100)    NOT NULL,
    state           VARCHAR(100)    NOT NULL,
    country         VARCHAR(100)    NOT NULL DEFAULT 'India',
    pin_code        VARCHAR(12)     NOT NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_buyer PRIMARY KEY (id),
    CONSTRAINT uk_buyer_email UNIQUE (email),
    CONSTRAINT chk_buyer_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE INDEX idx_buyer_status ON buyer (status);
CREATE INDEX idx_buyer_city ON buyer (city);
CREATE INDEX idx_buyer_state ON buyer (state);
CREATE INDEX idx_buyer_country ON buyer (country);
CREATE INDEX idx_buyer_company_name_lower ON buyer (LOWER(company_name));
CREATE INDEX idx_buyer_contact_person_lower ON buyer (LOWER(contact_person));
