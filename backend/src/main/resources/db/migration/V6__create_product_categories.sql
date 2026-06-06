CREATE TABLE product_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(80) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    remark VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_product_categories_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO product_categories (code, name, sort_order, enabled, remark, created_at, updated_at)
VALUES
    ('RICE', '稻谷', 10, TRUE, '系统默认种类', NOW(), NOW()),
    ('SEED', '稻种', 20, TRUE, '系统默认种类', NOW(), NOW()),
    ('FERTILIZER', '化肥', 30, TRUE, '系统默认种类', NOW(), NOW());
