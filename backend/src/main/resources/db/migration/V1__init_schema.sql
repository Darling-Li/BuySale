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
    counterparty_address VARCHAR(255),
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
    buyer_address VARCHAR(255),
    weight_jin DECIMAL(18, 2) NOT NULL,
    price_per_jin DECIMAL(18, 4) NOT NULL,
    total_amount DECIMAL(18, 2) NOT NULL,
    sold_at DATE NOT NULL,
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

CREATE INDEX idx_purchase_date ON purchase_orders (purchased_at);
CREATE INDEX idx_purchase_product ON purchase_orders (product_type, product_name);
CREATE INDEX idx_sale_date ON sale_orders (sold_at);
CREATE INDEX idx_sale_product ON sale_orders (product_type, product_name);
CREATE INDEX idx_inventory_product ON inventory_items (product_type, product_name);
CREATE INDEX idx_inventory_transaction_business ON inventory_transactions (business_type, business_id);

CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL,
    role_name VARCHAR(40) NOT NULL,
    method VARCHAR(16) NOT NULL,
    path VARCHAR(255) NOT NULL,
    action_name VARCHAR(120) NOT NULL,
    status_code INT NOT NULL,
    ip_address VARCHAR(80),
    user_agent VARCHAR(255),
    occurred_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_logs_time ON audit_logs (occurred_at);
CREATE INDEX idx_audit_logs_user ON audit_logs (username);

INSERT INTO warehouses (name, address, contact_name, contact_phone, remark, created_at, updated_at)
VALUES ('主仓库', '请在系统中修改仓库地址', NULL, NULL, '系统默认仓库', NOW(), NOW());
