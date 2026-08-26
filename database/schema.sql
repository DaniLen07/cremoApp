CREATE DATABASE IF NOT EXISTS bdDeli;

USE bdDeli;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS daily_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    inventory_date DATE NOT NULL,
    initial_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uq_inventory_product_date UNIQUE (product_id, inventory_date)
);

CREATE TABLE IF NOT EXISTS sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    sale_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    seller_name VARCHAR(120) NOT NULL DEFAULT 'No especificado',
    payment_method VARCHAR(20) NOT NULL,
    CONSTRAINT fk_sale_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT chk_payment_method CHECK (
        payment_method IN ('EFECTIVO', 'NEQUI')
    ),
    CONSTRAINT chk_seller_name CHECK (
        seller_name IN (
            'Juan Diego',
            'Christopher',
            'Salomé',
            'Daniel',
            'Luisa',
            'Otro'
        )
    )
);