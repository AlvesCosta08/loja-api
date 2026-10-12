CREATE TABLE payments (
    id                   BIGSERIAL      PRIMARY KEY,
    order_id             BIGINT         NOT NULL,
    method               VARCHAR(20)    NOT NULL,
    status               VARCHAR(20)    NOT NULL,
    amount               NUMERIC(12, 2) NOT NULL,
    pix_copy_paste       VARCHAR(500),
    pix_qr_code_base64   TEXT,
    created_at           TIMESTAMP      NOT NULL,
    expires_at           TIMESTAMP,
    confirmed_at         TIMESTAMP,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
