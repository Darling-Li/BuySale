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
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_system_user_roles_user FOREIGN KEY (user_id) REFERENCES system_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_system_user_roles_role FOREIGN KEY (role_id) REFERENCES system_roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
