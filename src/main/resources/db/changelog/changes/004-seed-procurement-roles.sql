--liquibase formatted sql

--changeset procurement:004-seed-procurement-roles
INSERT INTO roles (name, description)
VALUES
    ('EMPLOYEE', 'Creates and tracks purchase requisitions'),
    ('SUPERVISOR', 'Approves submitted purchase requisitions'),
    ('MANAGER', 'Approves higher value purchase requisitions and QCF decisions'),
    ('DIRECTOR', 'Approves strategic or emergency purchase requisitions'),
    ('PURCHASING_STAFF', 'Runs RFQ, vendor quotation, QCF preparation, and vendor master workflows'),
    ('PURCHASING_MANAGER', 'Approves QCF and award decisions'),
    ('WAREHOUSE_STAFF', 'Records goods receipts against purchase orders'),
    ('FINANCE_STAFF', 'Creates and verifies invoices'),
    ('FINANCE_MANAGER', 'Reviews verified invoices and records payment')
ON CONFLICT (name) DO NOTHING;

WITH role_permission_map(role_name, permission_key) AS (
    VALUES
        ('EMPLOYEE', 'pr:read'),
        ('EMPLOYEE', 'pr:write'),

        ('SUPERVISOR', 'pr:read'),
        ('SUPERVISOR', 'pr:approve'),
        ('SUPERVISOR', 'audit:read'),

        ('MANAGER', 'pr:read'),
        ('MANAGER', 'pr:approve'),
        ('MANAGER', 'qcf:read'),
        ('MANAGER', 'qcf:approve'),
        ('MANAGER', 'audit:read'),

        ('DIRECTOR', 'pr:read'),
        ('DIRECTOR', 'pr:approve'),
        ('DIRECTOR', 'audit:read'),

        ('PURCHASING_STAFF', 'pr:read'),
        ('PURCHASING_STAFF', 'rfq:read'),
        ('PURCHASING_STAFF', 'rfq:write'),
        ('PURCHASING_STAFF', 'qcf:read'),
        ('PURCHASING_STAFF', 'qcf:write'),
        ('PURCHASING_STAFF', 'vendors:read'),
        ('PURCHASING_STAFF', 'vendors:write'),
        ('PURCHASING_STAFF', 'po:read'),
        ('PURCHASING_STAFF', 'audit:read'),

        ('PURCHASING_MANAGER', 'rfq:read'),
        ('PURCHASING_MANAGER', 'qcf:read'),
        ('PURCHASING_MANAGER', 'qcf:approve'),
        ('PURCHASING_MANAGER', 'qcf:write'),
        ('PURCHASING_MANAGER', 'po:read'),
        ('PURCHASING_MANAGER', 'audit:read'),

        ('WAREHOUSE_STAFF', 'po:read'),
        ('WAREHOUSE_STAFF', 'warehouse:read'),
        ('WAREHOUSE_STAFF', 'warehouse:write'),
        ('WAREHOUSE_STAFF', 'audit:read'),

        ('FINANCE_STAFF', 'po:read'),
        ('FINANCE_STAFF', 'invoices:read'),
        ('FINANCE_STAFF', 'invoices:write'),
        ('FINANCE_STAFF', 'invoices:verify'),
        ('FINANCE_STAFF', 'audit:read'),

        ('FINANCE_MANAGER', 'invoices:read'),
        ('FINANCE_MANAGER', 'invoices:write'),
        ('FINANCE_MANAGER', 'invoices:verify'),
        ('FINANCE_MANAGER', 'audit:read')
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM role_permission_map rpm
JOIN roles r ON r.name = rpm.role_name
JOIN permissions p ON p.permission_key = rpm.permission_key
ON CONFLICT (role_id, permission_id) DO NOTHING;

--rollback DELETE FROM role_permissions WHERE role_id IN (SELECT id FROM roles WHERE name IN ('EMPLOYEE', 'SUPERVISOR', 'MANAGER', 'DIRECTOR', 'PURCHASING_STAFF', 'PURCHASING_MANAGER', 'WAREHOUSE_STAFF', 'FINANCE_STAFF', 'FINANCE_MANAGER'));
--rollback DELETE FROM roles WHERE name IN ('EMPLOYEE', 'SUPERVISOR', 'MANAGER', 'DIRECTOR', 'PURCHASING_STAFF', 'PURCHASING_MANAGER', 'WAREHOUSE_STAFF', 'FINANCE_STAFF', 'FINANCE_MANAGER');
