CREATE TABLE sale_settlements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_order_id BIGINT NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    settled_at DATETIME NOT NULL,
    remark VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_sale_settlement_order FOREIGN KEY (sale_order_id) REFERENCES sale_orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sale_settlements_order ON sale_settlements (sale_order_id);
CREATE INDEX idx_sale_settlements_time ON sale_settlements (settled_at);

INSERT INTO sale_settlements (sale_order_id, amount, channel, settled_at, remark, created_at, updated_at)
SELECT id, total_amount, 'HISTORY', CONCAT(sold_at, ' 00:00:00'), '历史已结账记录', NOW(), NOW()
FROM sale_orders
WHERE settled = TRUE;
