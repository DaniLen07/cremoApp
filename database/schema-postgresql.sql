CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS daily_inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products (id),
    inventory_date DATE NOT NULL,
    initial_quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    arequipe_quantity INTEGER NOT NULL DEFAULT 0,
    powdered_milk_quantity INTEGER NOT NULL DEFAULT 0,
    raisins_quantity INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_inventory_product_date UNIQUE (product_id, inventory_date)
);

CREATE TABLE IF NOT EXISTS sellers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(40) NOT NULL,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_seller_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS sales (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products (id),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    total NUMERIC(12, 2) NOT NULL,
    sale_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    seller_name VARCHAR(120) NOT NULL DEFAULT 'No especificado',
    payment_method VARCHAR(20) NOT NULL,
    arequipe INTEGER NOT NULL DEFAULT 0,
    powdered_milk INTEGER NOT NULL DEFAULT 0,
    raisins INTEGER NOT NULL DEFAULT 0,
    toppings_total NUMERIC(12, 2) NOT NULL DEFAULT 0,
    CONSTRAINT chk_payment_method CHECK (
        payment_method IN ('EFECTIVO', 'NEQUI')
    )
);