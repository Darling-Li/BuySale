CREATE TABLE product_units (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL,
    unit_to_jin DECIMAL(18, 4) NOT NULL,
    system_builtin BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    remark VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_product_units_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO product_units (name, unit_to_jin, system_builtin, sort_order, enabled, remark, created_at, updated_at)
VALUES
    ('斤', 1.0000, TRUE, 10, TRUE, '系统固定单位', NOW(), NOW()),
    ('公斤', 2.0000, TRUE, 20, TRUE, '系统固定单位', NOW(), NOW()),
    ('吨', 2000.0000, TRUE, 30, TRUE, '系统固定单位', NOW(), NOW());
