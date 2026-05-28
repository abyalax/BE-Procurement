--liquibase formatted sql

--changeset procurement:005-seed-role-users
INSERT INTO users (name, email, password, status)
VALUES
    ('Default User', 'user@procurement.local', '$2a$10$hI0nw9xV7JO4GOFS8mGxjuuTj1b1q4ECeFe8QYEPiZJqGgIGE671m', 'ACTIVE'),
    ('Employee User', 'employee@procurement.local', '$2a$10$hI0nw9xV7JO4GOFS8mGxjuuTj1b1q4ECeFe8QYEPiZJqGgIGE671m', 'ACTIVE'),
    ('Supervisor User', 'supervisor@procurement.local', '$2a$10$hI0nw9xV7JO4GOFS8mGxjuuTj1b1q4ECeFe8QYEPiZJqGgIGE671m', 'ACTIVE'),
    ('Manager User', 'manager@procurement.local', '$2a$10$hI0nw9xV7JO4GOFS8mGxjuuTj1b1q4ECeFe8QYEPiZJqGgIGE671m', 'ACTIVE'),
    ('Director User', 'director@procurement.local', '$2a$10$hI0nw9xV7JO4GOFS8mGxjuuTj1b1q4ECeFe8QYEPiZJqGgIGE671m', 'ACTIVE'),
    ('Purchasing Staff User', 'purchasing-staff@procurement.local', '$2a$10$hI0nw9xV7JO4GOFS8mGxjuuTj1b1q4ECeFe8QYEPiZJqGgIGE671m', 'ACTIVE'),
    ('Purchasing Manager User', 'purchasing-manager@procurement.local', '$2a$10$hI0nw9xV7JO4GOFS8mGxjuuTj1b1q4ECeFe8QYEPiZJqGgIGE671m', 'ACTIVE'),
    ('Warehouse Staff User', 'warehouse-staff@procurement.local', '$2a$10$hI0nw9xV7JO4GOFS8mGxjuuTj1b1q4ECeFe8QYEPiZJqGgIGE671m', 'ACTIVE'),
    ('Finance Staff User', 'finance-staff@procurement.local', '$2a$10$hI0nw9xV7JO4GOFS8mGxjuuTj1b1q4ECeFe8QYEPiZJqGgIGE671m', 'ACTIVE'),
    ('Finance Manager User', 'finance-manager@procurement.local', '$2a$10$hI0nw9xV7JO4GOFS8mGxjuuTj1b1q4ECeFe8QYEPiZJqGgIGE671m', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'USER'
WHERE u.email = 'user@procurement.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'EMPLOYEE'
WHERE u.email = 'employee@procurement.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'SUPERVISOR'
WHERE u.email = 'supervisor@procurement.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'MANAGER'
WHERE u.email = 'manager@procurement.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'DIRECTOR'
WHERE u.email = 'director@procurement.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'PURCHASING_STAFF'
WHERE u.email = 'purchasing-staff@procurement.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'PURCHASING_MANAGER'
WHERE u.email = 'purchasing-manager@procurement.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'WAREHOUSE_STAFF'
WHERE u.email = 'warehouse-staff@procurement.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'FINANCE_STAFF'
WHERE u.email = 'finance-staff@procurement.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'FINANCE_MANAGER'
WHERE u.email = 'finance-manager@procurement.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

--rollback DELETE FROM user_roles WHERE user_id IN (
--rollback   SELECT id FROM users WHERE email IN (
--rollback     'user@procurement.local',
--rollback     'employee@procurement.local',
--rollback     'supervisor@procurement.local',
--rollback     'manager@procurement.local',
--rollback     'director@procurement.local',
--rollback     'purchasing-staff@procurement.local',
--rollback     'purchasing-manager@procurement.local',
--rollback     'warehouse-staff@procurement.local',
--rollback     'finance-staff@procurement.local',
--rollback     'finance-manager@procurement.local'
--rollback   )
--rollback );
--rollback DELETE FROM users WHERE email IN (
--rollback   'user@procurement.local',
--rollback   'employee@procurement.local',
--rollback   'supervisor@procurement.local',
--rollback   'manager@procurement.local',
--rollback   'director@procurement.local',
--rollback   'purchasing-staff@procurement.local',
--rollback   'purchasing-manager@procurement.local',
--rollback   'warehouse-staff@procurement.local',
--rollback   'finance-staff@procurement.local',
--rollback   'finance-manager@procurement.local'
--rollback );
