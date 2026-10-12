ALTER TABLE payments ADD COLUMN gateway_ref   VARCHAR(100);
ALTER TABLE payments ADD COLUMN client_secret VARCHAR(255);

CREATE INDEX idx_payments_gateway_ref ON payments(gateway_ref);
