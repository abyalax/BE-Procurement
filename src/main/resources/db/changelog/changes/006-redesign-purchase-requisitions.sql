--liquibase formatted sql

--changeset procurement:006-redesign-purchase-requisitions
ALTER TABLE purchase_requisitions
    ADD COLUMN IF NOT EXISTS pr_number VARCHAR(80),
    ADD COLUMN IF NOT EXISTS total_estimated_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS required_date DATE,
    ADD COLUMN IF NOT EXISTS procurement_review_notes TEXT;

ALTER TABLE purchase_requisitions
    DROP COLUMN IF EXISTS amount,
    DROP COLUMN IF EXISTS quantity;

CREATE UNIQUE INDEX IF NOT EXISTS uk_purchase_requisitions_pr_number
    ON purchase_requisitions(pr_number)
    WHERE pr_number IS NOT NULL;

CREATE TABLE IF NOT EXISTS purchase_requisition_items (
    id BIGSERIAL PRIMARY KEY,
    pr_id BIGINT NOT NULL REFERENCES purchase_requisitions(id) ON DELETE CASCADE,
    line_no INTEGER NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    item_name VARCHAR(180) NOT NULL,
    description TEXT,
    specification TEXT,
    quantity NUMERIC(18,2) NOT NULL,
    unit_of_measure VARCHAR(40) NOT NULL,
    estimated_unit_price NUMERIC(18,2) NOT NULL DEFAULT 0,
    estimated_total_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    required_date DATE,
    delivery_location VARCHAR(180),
    budget_code VARCHAR(80),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_pr_item_line UNIQUE (pr_id, line_no),
    CONSTRAINT chk_pr_item_type CHECK (item_type IN ('GOODS', 'SERVICE')),
    CONSTRAINT chk_pr_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_pr_item_estimated_unit_price CHECK (estimated_unit_price >= 0)
);

CREATE TABLE IF NOT EXISTS rfq_items (
    id BIGSERIAL PRIMARY KEY,
    rfq_id BIGINT NOT NULL REFERENCES rfqs(id) ON DELETE CASCADE,
    pr_item_id BIGINT REFERENCES purchase_requisition_items(id),
    line_no INTEGER NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    item_name VARCHAR(180) NOT NULL,
    description TEXT,
    specification TEXT,
    quantity NUMERIC(18,2) NOT NULL,
    unit_of_measure VARCHAR(40) NOT NULL,
    required_date DATE,
    delivery_location VARCHAR(180),
    budget_code VARCHAR(80),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_rfq_item_line UNIQUE (rfq_id, line_no),
    CONSTRAINT chk_rfq_item_type CHECK (item_type IN ('GOODS', 'SERVICE')),
    CONSTRAINT chk_rfq_item_quantity CHECK (quantity > 0)
);

CREATE INDEX IF NOT EXISTS idx_pr_items_pr ON purchase_requisition_items(pr_id);
CREATE INDEX IF NOT EXISTS idx_pr_items_type ON purchase_requisition_items(item_type);
CREATE INDEX IF NOT EXISTS idx_pr_items_required_date ON purchase_requisition_items(required_date);
CREATE INDEX IF NOT EXISTS idx_rfq_items_rfq ON rfq_items(rfq_id);
CREATE INDEX IF NOT EXISTS idx_rfq_items_pr_item ON rfq_items(pr_item_id);

--rollback DROP TABLE IF EXISTS rfq_items;
--rollback DROP TABLE IF EXISTS purchase_requisition_items;
--rollback DROP INDEX IF EXISTS uk_purchase_requisitions_pr_number;
--rollback ALTER TABLE purchase_requisitions DROP COLUMN IF EXISTS procurement_review_notes;
--rollback ALTER TABLE purchase_requisitions DROP COLUMN IF EXISTS required_date;
--rollback ALTER TABLE purchase_requisitions DROP COLUMN IF EXISTS total_estimated_amount;
--rollback ALTER TABLE purchase_requisitions DROP COLUMN IF EXISTS pr_number;
--rollback ALTER TABLE purchase_requisitions ADD COLUMN IF NOT EXISTS amount NUMERIC(18,2) NOT NULL DEFAULT 0;
--rollback ALTER TABLE purchase_requisitions ADD COLUMN IF NOT EXISTS quantity NUMERIC(18,2) NOT NULL DEFAULT 0;
