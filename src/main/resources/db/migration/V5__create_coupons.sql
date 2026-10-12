CREATE TABLE coupons (
    id               BIGSERIAL      PRIMARY KEY,
    code             VARCHAR(50)    NOT NULL UNIQUE,
    type             VARCHAR(20)    NOT NULL,
    discount_value   NUMERIC(12, 2) NOT NULL,
    min_order_value  NUMERIC(12, 2),
    max_uses         INTEGER,
    usage_count      INTEGER        NOT NULL DEFAULT 0,
    expires_at       TIMESTAMP,
    active           BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_coupons_code ON coupons(code);
