CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL,
    role_name VARCHAR(40) NOT NULL,
    method VARCHAR(16) NOT NULL,
    path VARCHAR(255) NOT NULL,
    action_name VARCHAR(120) NOT NULL,
    status_code INT NOT NULL,
    ip_address VARCHAR(80),
    user_agent VARCHAR(255),
    occurred_at DATETIME NOT NULL,
    INDEX idx_audit_logs_time (occurred_at),
    INDEX idx_audit_logs_user (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
