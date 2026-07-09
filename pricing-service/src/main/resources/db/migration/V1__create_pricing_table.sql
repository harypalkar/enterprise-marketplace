CREATE TABLE pricing (
    id               UUID            NOT NULL,
    product_id       UUID            NOT NULL,
    seller_id        UUID            NOT NULL,
    unit_price       NUMERIC(19, 4)  NOT NULL,
    currency         CHAR(3)         NOT NULL DEFAULT 'INR',
    min_quantity     INTEGER         NOT NULL DEFAULT 1,
    discount_percent NUMERIC(5, 2),
    valid_from       TIMESTAMPTZ     NOT NULL,
    valid_to         TIMESTAMPTZ,
    status           VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMPTZ     NOT NULL,
    updated_at       TIMESTAMPTZ     NOT NULL,
    created_by       VARCHAR(128),
    updated_by       VARCHAR(128),
    version          BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_pricing PRIMARY KEY (id),
    CONSTRAINT chk_pricing_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_pricing_min_quantity CHECK (min_quantity >= 1),
    CONSTRAINT chk_pricing_discount_percent CHECK (discount_percent IS NULL OR (discount_percent >= 0 AND discount_percent <= 100)),
    CONSTRAINT chk_pricing_validity CHECK (valid_to IS NULL OR valid_to >= valid_from),
    CONSTRAINT chk_pricing_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_pricing_product_id ON pricing (product_id);
CREATE INDEX idx_pricing_seller_id ON pricing (seller_id);
CREATE INDEX idx_pricing_status ON pricing (status);
