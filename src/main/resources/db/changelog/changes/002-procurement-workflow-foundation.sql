--liquibase formatted sql

--changeset procurement:002-procurement-workflow-foundation
CREATE TABLE IF NOT EXISTS vendors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    performance_score NUMERIC(5,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchase_requisitions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    description TEXT,
    department VARCHAR(120) NOT NULL,
    requested_by VARCHAR(160) NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    quantity NUMERIC(18,2) NOT NULL,
    status VARCHAR(40) NOT NULL,
    stage VARCHAR(80) NOT NULL,
    emergency BOOLEAN NOT NULL DEFAULT FALSE,
    justification TEXT,
    cancellation_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rfqs (
    id BIGSERIAL PRIMARY KEY,
    pr_id BIGINT NOT NULL REFERENCES purchase_requisitions(id),
    title VARCHAR(180) NOT NULL,
    description TEXT,
    deadline TIMESTAMP NOT NULL,
    status VARCHAR(40) NOT NULL,
    stage VARCHAR(80) NOT NULL,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rfq_vendor_invitations (
    id BIGSERIAL PRIMARY KEY,
    rfq_id BIGINT NOT NULL REFERENCES rfqs(id) ON DELETE CASCADE,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    status VARCHAR(40) NOT NULL,
    response_notes TEXT,
    responded_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_rfq_vendor_invitation UNIQUE (rfq_id, vendor_id)
);

CREATE TABLE IF NOT EXISTS vendor_quotations (
    id BIGSERIAL PRIMARY KEY,
    rfq_id BIGINT NOT NULL REFERENCES rfqs(id) ON DELETE CASCADE,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    version INTEGER NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    lead_time_days INTEGER NOT NULL,
    technical_score NUMERIC(5,2) NOT NULL DEFAULT 0,
    commercial_score NUMERIC(5,2) NOT NULL DEFAULT 0,
    status VARCHAR(40) NOT NULL,
    notes TEXT,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_vendor_quotation_version UNIQUE (rfq_id, vendor_id, version)
);

CREATE TABLE IF NOT EXISTS qcf_documents (
    id BIGSERIAL PRIMARY KEY,
    rfq_id BIGINT NOT NULL UNIQUE REFERENCES rfqs(id),
    recommended_vendor_id BIGINT REFERENCES vendors(id),
    status VARCHAR(40) NOT NULL,
    stage VARCHAR(80) NOT NULL,
    recommendation_notes TEXT,
    created_by VARCHAR(160) NOT NULL,
    approved_by VARCHAR(160),
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS qcf_lines (
    id BIGSERIAL PRIMARY KEY,
    qcf_id BIGINT NOT NULL REFERENCES qcf_documents(id) ON DELETE CASCADE,
    quotation_id BIGINT NOT NULL REFERENCES vendor_quotations(id),
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    amount NUMERIC(18,2) NOT NULL,
    lead_time_days INTEGER NOT NULL,
    technical_score NUMERIC(5,2) NOT NULL DEFAULT 0,
    commercial_score NUMERIC(5,2) NOT NULL DEFAULT 0,
    total_score NUMERIC(6,2) NOT NULL DEFAULT 0,
    rank_no INTEGER NOT NULL,
    recommendation BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    qcf_id BIGINT NOT NULL UNIQUE REFERENCES qcf_documents(id),
    rfq_id BIGINT NOT NULL REFERENCES rfqs(id),
    vendor_id BIGINT NOT NULL REFERENCES vendors(id),
    po_number VARCHAR(80) NOT NULL UNIQUE,
    amount NUMERIC(18,2) NOT NULL,
    quantity NUMERIC(18,2) NOT NULL,
    remaining_quantity NUMERIC(18,2) NOT NULL,
    status VARCHAR(40) NOT NULL,
    stage VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS goods_receipts (
    id BIGSERIAL PRIMARY KEY,
    po_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    received_quantity NUMERIC(18,2) NOT NULL,
    received_by VARCHAR(160) NOT NULL,
    notes TEXT,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL PRIMARY KEY,
    po_id BIGINT NOT NULL REFERENCES purchase_orders(id),
    invoice_number VARCHAR(100) NOT NULL UNIQUE,
    amount NUMERIC(18,2) NOT NULL,
    status VARCHAR(40) NOT NULL,
    match_status VARCHAR(40) NOT NULL,
    submitted_by VARCHAR(160) NOT NULL,
    verified_by VARCHAR(160),
    paid_by VARCHAR(160),
    notes TEXT,
    verified_at TIMESTAMP,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS approval_workflows (
    id BIGSERIAL PRIMARY KEY,
    document_type VARCHAR(40) NOT NULL,
    document_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_approval_workflow_document UNIQUE (document_type, document_id)
);

CREATE TABLE IF NOT EXISTS approval_steps (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT NOT NULL REFERENCES approval_workflows(id) ON DELETE CASCADE,
    sequence_no INTEGER NOT NULL,
    approver_role VARCHAR(80) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'WAITING',
    decided_by VARCHAR(160),
    decided_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_approval_step_sequence UNIQUE (workflow_id, sequence_no)
);

CREATE TABLE IF NOT EXISTS audit_activities (
    id BIGSERIAL PRIMARY KEY,
    document_type VARCHAR(40) NOT NULL,
    document_id BIGINT NOT NULL,
    activity_type VARCHAR(80) NOT NULL,
    actor VARCHAR(160),
    notes TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pr_status ON purchase_requisitions(status);
CREATE INDEX IF NOT EXISTS idx_rfqs_pr ON rfqs(pr_id);
CREATE INDEX IF NOT EXISTS idx_rfqs_status ON rfqs(status);
CREATE INDEX IF NOT EXISTS idx_quotations_rfq_vendor ON vendor_quotations(rfq_id, vendor_id);
CREATE INDEX IF NOT EXISTS idx_qcf_rfq ON qcf_documents(rfq_id);
CREATE INDEX IF NOT EXISTS idx_po_vendor ON purchase_orders(vendor_id);
CREATE INDEX IF NOT EXISTS idx_invoice_po ON invoices(po_id);
CREATE INDEX IF NOT EXISTS idx_audit_document ON audit_activities(document_type, document_id, created_at);

--rollback DROP TABLE IF EXISTS audit_activities;
--rollback DROP TABLE IF EXISTS approval_steps;
--rollback DROP TABLE IF EXISTS approval_workflows;
--rollback DROP TABLE IF EXISTS invoices;
--rollback DROP TABLE IF EXISTS goods_receipts;
--rollback DROP TABLE IF EXISTS purchase_orders;
--rollback DROP TABLE IF EXISTS qcf_lines;
--rollback DROP TABLE IF EXISTS qcf_documents;
--rollback DROP TABLE IF EXISTS vendor_quotations;
--rollback DROP TABLE IF EXISTS rfq_vendor_invitations;
--rollback DROP TABLE IF EXISTS rfqs;
--rollback DROP TABLE IF EXISTS purchase_requisitions;
--rollback DROP TABLE IF EXISTS vendors;
