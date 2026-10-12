CREATE TABLE products (
    id         BIGSERIAL      PRIMARY KEY,
    name       VARCHAR(255)   NOT NULL,
    price      NUMERIC(12, 2) NOT NULL,
    stock      INTEGER        NOT NULL,
    category   VARCHAR(255)   NOT NULL,
    image_url  VARCHAR(500)
);

CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_name ON products(name);
