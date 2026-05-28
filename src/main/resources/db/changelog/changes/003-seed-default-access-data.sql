--liquibase formatted sql

--changeset procurement:003-seed-default-access-data
INSERT INTO permissions (permission_key, name, description)
VALUES
    ('users:read', 'Read users', 'Read users'),
    ('users:write', 'Write users', 'Write users'),
    ('roles:read', 'Read roles', 'Read roles'),
    ('roles:write', 'Write roles', 'Write roles'),
    ('permissions:read', 'Read permissions', 'Read permissions'),
    ('pr:read', 'Read PR', 'Read PR'),
    ('pr:write', 'Write PR', 'Write PR'),
    ('pr:approve', 'Approve PR', 'Approve PR'),
    ('rfq:read', 'Read RFQ', 'Read RFQ'),
    ('rfq:write', 'Write RFQ', 'Write RFQ'),
    ('qcf:read', 'Read QCF', 'Read QCF'),
    ('qcf:write', 'Write QCF', 'Write QCF'),
    ('qcf:approve', 'Approve QCF', 'Approve QCF'),
    ('vendors:read', 'Read vendors', 'Read vendors'),
    ('vendors:write', 'Write vendors', 'Write vendors'),
    ('po:read', 'Read PO', 'Read PO'),
    ('po:write', 'Write PO', 'Write PO'),
    ('warehouse:read', 'Read warehouse', 'Read warehouse'),
    ('warehouse:write', 'Write warehouse', 'Write warehouse'),
    ('invoices:read', 'Read invoices', 'Read invoices'),
    ('invoices:write', 'Write invoices', 'Write invoices'),
    ('invoices:verify', 'Verify invoices', 'Verify invoices'),
    ('audit:read', 'Read audit', 'Read audit')
ON CONFLICT (permission_key) DO NOTHING;

INSERT INTO roles (name, description)
VALUES
    ('ADMIN', 'Full system administrator'),
    ('USER', 'Default authenticated user')
ON CONFLICT (name) DO NOTHING;

INSERT INTO vendors (name, email, status, performance_score)
VALUES
    ('Nusantara Office Supply', 'sales@nusantara-office.local', 'ACTIVE', 82.50),
    ('Metro Industrial Goods', 'rfq@metro-industrial.local', 'ACTIVE', 78.00),
    ('Prima Logistics Services', 'procurement@prima-logistics.local', 'ACTIVE', 88.25)
ON CONFLICT (email) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.permission_key = 'users:read'
WHERE r.name = 'USER'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO users (name, email, password, status)
VALUES (
    'System Administrator',
    'admin@procurement.local',
    '$2a$10$hI0nw9xV7JO4GOFS8mGxjuuTj1b1q4ECeFe8QYEPiZJqGgIGE671m',
    'ACTIVE'
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.email = 'admin@procurement.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

--rollback DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE email = 'admin@procurement.local');
--rollback DELETE FROM users WHERE email = 'admin@procurement.local';
--rollback DELETE FROM role_permissions WHERE role_id IN (SELECT id FROM roles WHERE name IN ('ADMIN', 'USER'));
--rollback DELETE FROM vendors WHERE email IN ('sales@nusantara-office.local', 'rfq@metro-industrial.local', 'procurement@prima-logistics.local');
--rollback DELETE FROM roles WHERE name IN ('ADMIN', 'USER');
--rollback DELETE FROM permissions WHERE permission_key IN ('users:read', 'users:write', 'roles:read', 'roles:write', 'permissions:read', 'pr:read', 'pr:write', 'pr:approve', 'rfq:read', 'rfq:write', 'qcf:read', 'qcf:write', 'qcf:approve', 'vendors:read', 'vendors:write', 'po:read', 'po:write', 'warehouse:read', 'warehouse:write', 'invoices:read', 'invoices:write', 'invoices:verify', 'audit:read');
