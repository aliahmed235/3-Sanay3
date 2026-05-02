INSERT INTO roles (name, description)
VALUES
    ('USER', 'Customer can make service requests'),
    ('SERVICE_PROVIDER', 'Service provider can accept requests and earn'),
    ('ADMIN', 'Admin can verify providers and manage platform')
ON DUPLICATE KEY UPDATE description = VALUES(description);
