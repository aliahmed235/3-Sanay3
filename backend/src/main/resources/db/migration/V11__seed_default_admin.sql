-- V11: Seed a default admin user
-- Password: Admin1234 (BCrypt encoded)
-- CHANGE THIS PASSWORD after first login in production!

INSERT INTO users (name, email, phone, password, is_active, created_at, updated_at)
VALUES (
    'System Admin',
    'admin@sany3.com',
    '01000000000',
    '$2a$10$pbEuofXWdWyHtEd3isHXlO3k.crZ2HrVhVeAV3XoYygpHvZU5hNCO',
    true,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE name = name;

-- Assign both USER and ADMIN roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@sany3.com' AND r.name = 'USER'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@sany3.com' AND r.name = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- Create customer profile for admin (required by the system)
INSERT INTO customer_profiles (user_id, created_at, updated_at)
SELECT u.id, NOW(), NOW()
FROM users u
WHERE u.email = 'admin@sany3.com'
AND NOT EXISTS (
    SELECT 1 FROM customer_profiles cp WHERE cp.user_id = u.id
);
