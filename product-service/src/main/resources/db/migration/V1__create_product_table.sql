CREATE TABLE product (
    id              UUID            NOT NULL,
    sku             VARCHAR(64)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    seller_id       UUID            NOT NULL,
    category_id     UUID,
    unit_price      NUMERIC(19, 4)  NOT NULL,
    currency        CHAR(3)         NOT NULL DEFAULT 'INR',
    min_order_quantity INTEGER      NOT NULL DEFAULT 1,
    unit_of_measure VARCHAR(32)     NOT NULL DEFAULT 'PCS',
    hsn_code        VARCHAR(16),
    gst_rate        NUMERIC(5, 2),
    status          VARCHAR(32)     NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_product PRIMARY KEY (id),
    CONSTRAINT uk_product_sku UNIQUE (sku),
    CONSTRAINT chk_product_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_product_min_order_quantity CHECK (min_order_quantity >= 1),
    CONSTRAINT chk_product_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE INDEX idx_product_seller_id ON product (seller_id);
CREATE INDEX idx_product_category_id ON product (category_id);
CREATE INDEX idx_product_status ON product (status);
CREATE INDEX idx_product_name_lower ON product (LOWER(name));
