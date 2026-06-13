CREATE TABLE system_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_system_roles_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE system_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(120) NOT NULL,
    display_name VARCHAR(80),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_system_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE system_user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_system_user_roles_role ON system_user_roles (role_id);

CREATE TABLE product_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(80) NOT NULL,
    system_builtin BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    remark VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_product_categories_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE warehouses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    address VARCHAR(255),
    contact_name VARCHAR(60),
    contact_phone VARCHAR(30),
    remark VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_type VARCHAR(32) NOT NULL,
    product_name VARCHAR(80) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    counterparty_name VARCHAR(80) NOT NULL,
    counterparty_phone VARCHAR(30),
    counterparty_province VARCHAR(40),
    counterparty_city VARCHAR(60),
    counterparty_county VARCHAR(80),
    counterparty_address_detail VARCHAR(255),
    quantity DECIMAL(18, 2) NOT NULL,
    unit_name VARCHAR(20) NOT NULL,
    unit_to_jin DECIMAL(18, 4) NOT NULL,
    unit_price DECIMAL(18, 4) NOT NULL,
    weight_jin DECIMAL(18, 2) NOT NULL,
    price_per_jin DECIMAL(18, 4) NOT NULL,
    total_amount DECIMAL(18, 2) NOT NULL,
    purchased_at DATE NOT NULL,
    remark VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sale_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_type VARCHAR(32) NOT NULL,
    product_name VARCHAR(80) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    buyer_name VARCHAR(80) NOT NULL,
    buyer_phone VARCHAR(30),
    buyer_province VARCHAR(40),
    buyer_city VARCHAR(60),
    buyer_county VARCHAR(80),
    buyer_address_detail VARCHAR(255),
    quantity DECIMAL(18, 2) NOT NULL,
    unit_name VARCHAR(20) NOT NULL,
    unit_to_jin DECIMAL(18, 4) NOT NULL,
    unit_price DECIMAL(18, 4) NOT NULL,
    weight_jin DECIMAL(18, 2) NOT NULL,
    price_per_jin DECIMAL(18, 4) NOT NULL,
    total_amount DECIMAL(18, 2) NOT NULL,
    sold_at DATE NOT NULL,
    remark VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE sale_settlements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_order_id BIGINT NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    settled_at DATETIME NOT NULL,
    remark VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL,
    product_type VARCHAR(32) NOT NULL,
    product_name VARCHAR(80) NOT NULL,
    quantity_jin DECIMAL(18, 2) NOT NULL,
    average_cost_per_jin DECIMAL(18, 4) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_inventory_scope UNIQUE (warehouse_id, product_type, product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_type VARCHAR(32) NOT NULL,
    business_type VARCHAR(32) NOT NULL,
    business_id BIGINT NOT NULL,
    product_type VARCHAR(32) NOT NULL,
    product_name VARCHAR(80) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    weight_jin DECIMAL(18, 2) NOT NULL,
    price_per_jin DECIMAL(18, 4) NOT NULL,
    occurred_at DATETIME NOT NULL,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL,
    role_name VARCHAR(40) NOT NULL,
    method VARCHAR(16) NOT NULL,
    path VARCHAR(255) NOT NULL,
    action_name VARCHAR(120) NOT NULL,
    request_params MEDIUMTEXT,
    status_code INT NOT NULL,
    ip_address VARCHAR(80),
    user_agent VARCHAR(255),
    occurred_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_purchase_date ON purchase_orders (purchased_at);
CREATE INDEX idx_purchase_product ON purchase_orders (product_type, product_name);
CREATE INDEX idx_purchase_warehouse ON purchase_orders (warehouse_id);
CREATE INDEX idx_purchase_phone ON purchase_orders (counterparty_phone);

CREATE INDEX idx_sale_date ON sale_orders (sold_at);
CREATE INDEX idx_sale_product ON sale_orders (product_type, product_name);
CREATE INDEX idx_sale_warehouse ON sale_orders (warehouse_id);
CREATE INDEX idx_sale_phone ON sale_orders (buyer_phone);

CREATE INDEX idx_sale_settlements_order ON sale_settlements (sale_order_id);
CREATE INDEX idx_sale_settlements_time ON sale_settlements (settled_at);

CREATE INDEX idx_inventory_product ON inventory_items (product_type, product_name);
CREATE INDEX idx_inventory_warehouse ON inventory_items (warehouse_id);
CREATE INDEX idx_inventory_transaction_business ON inventory_transactions (business_type, business_id);
CREATE INDEX idx_inventory_transaction_time ON inventory_transactions (occurred_at);

CREATE INDEX idx_audit_logs_time ON audit_logs (occurred_at);
CREATE INDEX idx_audit_logs_user ON audit_logs (username);

INSERT INTO system_roles (code, name, description, enabled, created_at, updated_at)
VALUES
    ('ADMIN', '管理员', '可新增、修改、删除业务数据，并查看操作日志和维护系统用户。', TRUE, NOW(), NOW()),
    ('USER', '普通角色', '只能查询看板、采购、销售、库存、仓库等业务数据。', TRUE, NOW(), NOW());

INSERT INTO system_users (username, password_hash, display_name, enabled, created_at, updated_at)
VALUES
    ('admin', '$2y$10$5IqYCL34bnr7iq/Im6s8fOyVGWetgnL8hzS/eX1LD3CSMb7D01iKW', '系统管理员', TRUE, NOW(), NOW()),
    ('user', '$2y$10$l6Y5P6ntc8xhHii7wXVA.e3KipG2.IdwK4BdBjhxdM4mRvaGLAUzW', '普通用户', TRUE, NOW(), NOW());

INSERT INTO system_user_roles (user_id, role_id)
SELECT u.id, r.id
FROM system_users u
JOIN system_roles r ON r.code = 'ADMIN'
WHERE u.username = 'admin';

INSERT INTO system_user_roles (user_id, role_id)
SELECT u.id, r.id
FROM system_users u
JOIN system_roles r ON r.code = 'USER'
WHERE u.username = 'user';

INSERT INTO product_categories (code, name, system_builtin, sort_order, enabled, remark, created_at, updated_at)
VALUES
    ('RICE', '稻谷', TRUE, 10, TRUE, '系统默认种类', NOW(), NOW()),
    ('FERTILIZER', '化肥', TRUE, 20, TRUE, '系统默认种类', NOW(), NOW()),
    ('SEED', '种子', TRUE, 30, TRUE, '系统默认种类', NOW(), NOW()),
    ('WHEAT', '小麦', TRUE, 40, TRUE, '系统默认种类', NOW(), NOW());

INSERT INTO product_units (name, unit_to_jin, system_builtin, sort_order, enabled, remark, created_at, updated_at)
VALUES
    ('斤', 1.0000, TRUE, 10, TRUE, '系统固定单位', NOW(), NOW()),
    ('吨', 2000.0000, TRUE, 20, TRUE, '系统固定单位', NOW(), NOW()),
    ('kg', 2.0000, TRUE, 30, TRUE, '系统固定单位', NOW(), NOW());

INSERT INTO warehouses (name, address, contact_name, contact_phone, remark, created_at, updated_at)
VALUES ('主仓库', '请在系统中修改仓库地址', NULL, NULL, '系统默认仓库', NOW(), NOW());
