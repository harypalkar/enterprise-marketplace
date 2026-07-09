CREATE TABLE seller (
    id              UUID            NOT NULL,
    company_name    VARCHAR(255)    NOT NULL,
    trade_name      VARCHAR(255)    NOT NULL,
    gstin           VARCHAR(15)     NOT NULL,
    pan             VARCHAR(10)     NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    phone           VARCHAR(10)     NOT NULL,
    city            VARCHAR(128)    NOT NULL,
    state           VARCHAR(128)    NOT NULL,
    country         VARCHAR(128)    NOT NULL DEFAULT 'India',
    pin_code        VARCHAR(10)     NOT NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_seller PRIMARY KEY (id),
    CONSTRAINT uk_seller_gstin UNIQUE (gstin),
    CONSTRAINT chk_seller_status CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'ARCHIVED'))
);

CREATE INDEX idx_seller_status ON seller (status);
CREATE INDEX idx_seller_company_name_lower ON seller (LOWER(company_name));
CREATE INDEX idx_seller_trade_name_lower ON seller (LOWER(trade_name));
