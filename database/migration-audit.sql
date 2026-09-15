CREATE TABLE IF NOT EXISTS sale_audit (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    actor_username VARCHAR(80) NOT NULL,
    seller_name VARCHAR(120) NOT NULL,
    occurred_at TIMESTAMP NOT NULL
);