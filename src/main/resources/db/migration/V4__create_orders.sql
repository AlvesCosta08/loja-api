CREATE TABLE orders (
    id          BIGSERIAL      PRIMARY KEY,
    user_id     BIGINT         NOT NULL,
    status      VARCHAR(20)    NOT NULL,
    total       NUMERIC(12, 2) NOT NULL,
    created_at  TIMESTAMP      NOT NULL,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE order_items (
    id            BIGSERIAL      PRIMARY KEY,
    order_id      BIGINT         NOT NULL,
    product_id    BIGINT,
    product_name  VARCHAR(255)   NOT NULL,
    unit_price    NUMERIC(12, 2) NOT NULL,
    quantity      INTEGER        NOT NULL,
    subtotal      NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);
