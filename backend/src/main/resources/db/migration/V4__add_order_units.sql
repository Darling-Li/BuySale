ALTER TABLE purchase_orders
    ADD COLUMN quantity DECIMAL(18, 2) NULL AFTER counterparty_address,
    ADD COLUMN unit_name VARCHAR(20) NOT NULL DEFAULT '斤' AFTER quantity,
    ADD COLUMN unit_to_jin DECIMAL(18, 4) NOT NULL DEFAULT 1.0000 AFTER unit_name,
    ADD COLUMN unit_price DECIMAL(18, 4) NULL AFTER unit_to_jin;

UPDATE purchase_orders
SET quantity = weight_jin,
    unit_price = price_per_jin
WHERE quantity IS NULL;

ALTER TABLE purchase_orders
    MODIFY COLUMN quantity DECIMAL(18, 2) NOT NULL,
    MODIFY COLUMN unit_price DECIMAL(18, 4) NOT NULL;

ALTER TABLE sale_orders
    ADD COLUMN quantity DECIMAL(18, 2) NULL AFTER buyer_address,
    ADD COLUMN unit_name VARCHAR(20) NOT NULL DEFAULT '斤' AFTER quantity,
    ADD COLUMN unit_to_jin DECIMAL(18, 4) NOT NULL DEFAULT 1.0000 AFTER unit_name,
    ADD COLUMN unit_price DECIMAL(18, 4) NULL AFTER unit_to_jin;

UPDATE sale_orders
SET quantity = weight_jin,
    unit_price = price_per_jin
WHERE quantity IS NULL;

ALTER TABLE sale_orders
    MODIFY COLUMN quantity DECIMAL(18, 2) NOT NULL,
    MODIFY COLUMN unit_price DECIMAL(18, 4) NOT NULL;
