CREATE TABLE category (
    id              UUID            NOT NULL,
    slug            VARCHAR(120)    NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    parent_id       UUID,
    display_order   INTEGER         NOT NULL DEFAULT 0,
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL,
    created_by      VARCHAR(128),
    updated_by      VARCHAR(128),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT uk_category_slug UNIQUE (slug),
    CONSTRAINT chk_category_display_order CHECK (display_order >= 0),
    CONSTRAINT chk_category_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_category_parent_id ON category (parent_id);
CREATE INDEX idx_category_status ON category (status);
CREATE INDEX idx_category_name_lower ON category (LOWER(name));
