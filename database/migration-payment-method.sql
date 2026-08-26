USE bdDeli;

ALTER TABLE sales DROP CHECK chk_payment_method;

ALTER TABLE sales
ADD CONSTRAINT chk_payment_method CHECK (
    payment_method IN ('EFECTIVO', 'NEQUI')
);

UPDATE sales
SET
    seller_name = 'Otro'
WHERE
    seller_name NOT IN(
        'Juan Diego',
        'Christopher',
        'Salomé',
        'Daniel',
        'Luisa',
        'Otro'
    );

ALTER TABLE sales
ADD CONSTRAINT chk_seller_name CHECK (
    seller_name IN (
        'Juan Diego',
        'Christopher',
        'Salomé',
        'Daniel',
        'Luisa',
        'Otro'
    )
);