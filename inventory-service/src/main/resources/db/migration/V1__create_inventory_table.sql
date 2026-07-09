CREATE TABLE inventory (
    id UUID NOT NULL,
    product_id UUID NOT NULL,
    seller_id UUID NOT NULL,
    quantity_available INTEGER NOT NULL DEFAULT 0,
    quantity_reserved INTEGER NOT NULL DEFAULT 0,
    reorder_level INTEGER NOT NULL DEFAULT 0,
    warehouse_code VARCHAR(32),
    status VARCHAR(32) NOT NULL DEFAULT 'IN_STOCK',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_inventory PRIMARY KEY (id),
    CONSTRAINT uk_inventory_product_seller_warehouse UNIQUE (product_id, seller_id, warehouse_code),
    CONSTRAINT chk_inventory_quantity_available CHECK (quantity_available >= 0),
    CONSTRAINT chk_inventory_quantity_reserved CHECK (quantity_reserved >= 0),
    CONSTRAINT chk_inventory_reorder_level CHECK (reorder_level >= 0),
    CONSTRAINT chk_inventory_status CHECK (status IN ('IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK'))
);

CREATE INDEX idx_inventory_product_id ON inventory (product_id);
CREATE INDEX idx_inventory_seller_id ON inventory (seller_id);
CREATE INDEX idx_inventory_status ON inventory (status);
