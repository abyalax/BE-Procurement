# Database

This document describes the database shape and startup behavior implemented in the backend today.
It only documents features that actually exist in the codebase.

## Runtime Behavior

- PostgreSQL is the primary database.
- JPA runs with `ddl-auto: validate`, so entity mappings must match the database schema.
- Liquibase is disabled during normal application startup by default.
- Database migration and seed data are executed manually through the maintenance command.

## Migration Workflow

### Create

Create a new formatted SQL file in `src/main/resources/db/changelog/changes`.

Recommended pattern:

```txt
003-add-something.sql
```

Rules:

- Use the next unused numeric prefix.
- Start the file with `--liquibase formatted sql` and a unique `--changeset` line.
- Add rollback statements inline with `--rollback`.
- Keep the filename stable after it has been committed.
- Do not edit a migration that has already been applied to a shared database.

Example changeset header:

```sql
--liquibase formatted sql
--changeset procurement:003-add-something
--rollback DROP TABLE IF EXISTS example_table;
```

### Up

`Up` is run manually through the maintenance command.

Run schema migration with:

```bash
./mvnw spring-boot:run -Dspring-boot.run.main-class=com.procurement.tools.DatabaseMaintenanceApplication -Dspring-boot.run.arguments="--maintenance.command=migrate"
```

On Windows:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.main-class=com.procurement.tools.DatabaseMaintenanceApplication -Dspring-boot.run.arguments="--maintenance.command=migrate"
```

Schema changelog files are:

1. `001-init-auth-users-roles-permissions.sql`
2. `002-procurement-workflow-foundation.sql`
3. `006-redesign-purchase-requisitions.sql`

### Down

Liquibase rollback is available inline inside each `.sql` file.

To run a rollback manually, either:

1. Run a database rollback procedure against the same changeset file.
2. Reset the local database and rerun `migrate` if you only need a clean local state.

## Seeder Workflow

The application seeds default access data through Liquibase, but only when you run the maintenance command.

Current behavior:

- The seeder does not run during normal application startup.
- It is idempotent because each record is created only when it does not already exist.
- There is no dedicated seed-status table or `inserted` flag in the current code.

### Create Seeder

Seeder logic now lives in Liquibase formatted SQL, not in a Spring `CommandLineRunner`.

To add new seeded data:

1. Add a new Liquibase SQL changeset under `src/main/resources/db/changelog/changes`.
2. Include the file from `db.changelog-seed.yml`.
3. Keep inserts idempotent with `ON CONFLICT DO NOTHING` or equivalent predicates.
4. Add inline `--rollback` statements that delete the seeded rows in reverse order.

### Run Seeder

Run seed data manually with:

```bash
./mvnw spring-boot:run -Dspring-boot.run.main-class=com.procurement.tools.DatabaseMaintenanceApplication -Dspring-boot.run.arguments="--maintenance.command=seed"
```

On Windows:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.main-class=com.procurement.tools.DatabaseMaintenanceApplication -Dspring-boot.run.arguments="--maintenance.command=seed"
```

If you need both schema and seed in one step, use:

```bash
./mvnw spring-boot:run -Dspring-boot.run.main-class=com.procurement.tools.DatabaseMaintenanceApplication -Dspring-boot.run.arguments="--maintenance.command=all"
```

After `migrate` and `seed` complete, start the main backend normally with:

```bash
./mvnw spring-boot:run
```

Seeder data is currently defined in `003-seed-default-access-data.sql`, `004-seed-procurement-roles.sql`, and `005-seed-role-users.sql`.

### Seeded Data

Permissions inserted by the seed changeset:

- `users:read`
- `users:write`
- `roles:read`
- `roles:write`
- `permissions:read`
- `pr:read`
- `pr:write`
- `pr:approve`
- `rfq:read`
- `rfq:write`
- `qcf:read`
- `qcf:write`
- `qcf:approve`
- `vendors:read`
- `vendors:write`
- `po:read`
- `po:write`
- `warehouse:read`
- `warehouse:write`
- `invoices:read`
- `invoices:write`
- `invoices:verify`
- `audit:read`

Roles inserted by the seed changeset:

- `ADMIN`
- `USER`

Default admin user:

- Email: `admin@procurement.local`
- Password: `password`
- Name: `System Administrator`
- Status: `ACTIVE`

Default role users:

- `user@procurement.local` for `USER`
- `employee@procurement.local` for `EMPLOYEE`
- `supervisor@procurement.local` for `SUPERVISOR`
- `manager@procurement.local` for `MANAGER`
- `director@procurement.local` for `DIRECTOR`
- `purchasing-staff@procurement.local` for `PURCHASING_STAFF`
- `purchasing-manager@procurement.local` for `PURCHASING_MANAGER`
- `warehouse-staff@procurement.local` for `WAREHOUSE_STAFF`
- `finance-staff@procurement.local` for `FINANCE_STAFF`
- `finance-manager@procurement.local` for `FINANCE_MANAGER`

All seeded users use the `password` password.

Default vendors inserted by the seed changeset:

- `Nusantara Office Supply`
- `Metro Industrial Goods`
- `Prima Logistics Services`

## Schema Overview

### Access Control

#### `users`

Stores application users.

Key fields:

- `name`
- `email` unique
- `password`
- `status`
- `created_at`
- `updated_at`

Indexes:

- `idx_users_email`

#### `roles`

Stores roles such as `ADMIN` and `USER`.

Key fields:

- `name` unique
- `description`
- timestamps

#### `permissions`

Stores permission definitions.

Key fields:

- `permission_key` unique
- `name` unique
- `description`
- timestamps

#### `role_permissions`

Join table between roles and permissions.

Constraints:

- `role_id` references `roles(id)` with cascade delete
- `permission_id` references `permissions(id)` with cascade delete
- unique pair on `(role_id, permission_id)`

Indexes:

- `idx_role_permissions_role`
- `idx_role_permissions_permission`

#### `user_roles`

Join table between users and roles.

Constraints:

- composite primary key `(user_id, role_id)`
- both columns reference their parent tables with cascade delete

Indexes:

- `idx_user_roles_user`
- `idx_user_roles_role`

### Procurement Master Data

#### `vendors`

Stores vendor master records.

Key fields:

- `name`
- `email` unique
- `status`
- `performance_score`
- timestamps

#### `purchase_requisitions`

Stores purchase requisition document headers.

Key fields:

- `pr_number` unique when present
- `title`
- `description`
- `department`
- `requested_by`
- `total_estimated_amount`
- `status`
- `stage`
- `emergency`
- `required_date`
- `justification`
- `procurement_review_notes`
- `cancellation_reason`
- timestamps

Indexes:

- `idx_pr_status`

#### `purchase_requisition_items`

Stores requested goods and services for a purchase requisition.

Key fields:

- `pr_id` references `purchase_requisitions(id)` with cascade delete
- `line_no`
- `item_type` as `GOODS` or `SERVICE`
- `item_name`
- `description`
- `specification`
- `quantity`
- `unit_of_measure`
- `estimated_unit_price`
- `estimated_total_amount`
- `required_date`
- `delivery_location`
- `budget_code`
- `notes`
- timestamps

Constraints:

- unique pair on `(pr_id, line_no)`
- quantity must be greater than zero
- estimated unit price must not be negative

Indexes:

- `idx_pr_items_pr`
- `idx_pr_items_type`
- `idx_pr_items_required_date`

#### `rfqs`

Stores request-for-quotation records linked to a purchase requisition.

Key fields:

- `pr_id` references `purchase_requisitions(id)`
- `title`
- `description`
- `deadline`
- `status`
- `stage`
- `failure_reason`
- timestamps

Indexes:

- `idx_rfqs_pr`
- `idx_rfqs_status`

#### `rfq_items`

Stores RFQ line snapshots copied from PR items when an RFQ is created.

Constraints:

- `rfq_id` references `rfqs(id)` with cascade delete
- `pr_item_id` references `purchase_requisition_items(id)`
- unique pair on `(rfq_id, line_no)`

Indexes:

- `idx_rfq_items_rfq`
- `idx_rfq_items_pr_item`

#### `rfq_vendor_invitations`

Stores vendor invitations for an RFQ.

Constraints:

- `rfq_id` references `rfqs(id)` with cascade delete
- `vendor_id` references `vendors(id)`
- unique pair on `(rfq_id, vendor_id)`

#### `vendor_quotations`

Stores vendor quotations submitted against an RFQ.

Constraints:

- `rfq_id` references `rfqs(id)` with cascade delete
- `vendor_id` references `vendors(id)`
- unique pair on `(rfq_id, vendor_id, version)`

Indexes:

- `idx_quotations_rfq_vendor`

#### `qcf_documents`

Stores quotation comparison forms.

Constraints:

- `rfq_id` is unique and references `rfqs(id)`
- `recommended_vendor_id` references `vendors(id)`

Indexes:

- `idx_qcf_rfq`

#### `qcf_lines`

Stores the line items and scoring rows for a QCF.

Constraints:

- `qcf_id` references `qcf_documents(id)` with cascade delete
- `quotation_id` references `vendor_quotations(id)`
- `vendor_id` references `vendors(id)`

#### `purchase_orders`

Stores purchase orders created from an approved QCF.

Constraints:

- `qcf_id` is unique and references `qcf_documents(id)`
- `rfq_id` references `rfqs(id)`
- `vendor_id` references `vendors(id)`
- `po_number` is unique

Indexes:

- `idx_po_vendor`

#### `goods_receipts`

Stores warehouse receipt records linked to a purchase order.

Constraints:

- `po_id` references `purchase_orders(id)` with cascade delete

#### `invoices`

Stores invoices linked to a purchase order.

Constraints:

- `po_id` references `purchase_orders(id)`
- `invoice_number` is unique

Indexes:

- `idx_invoice_po`

### Workflow and Audit

#### `approval_workflows`

Stores approval workflow headers for a document.

Constraints:

- unique pair on `(document_type, document_id)`

#### `approval_steps`

Stores ordered approval steps inside a workflow.

Constraints:

- `workflow_id` references `approval_workflows(id)` with cascade delete
- unique pair on `(workflow_id, sequence_no)`

#### `audit_activities`

Stores immutable audit activity records.

Indexes:

- `idx_audit_document`

## Mapping Notes

- Entity classes live under `be/src/main/java/com/procurement/modules/**/entities`.
- Table names in the entities match the Liquibase migrations.
- The schema is intentionally forward-only in code. Any structural rollback should be handled with a new migration.

_Last Updated 29 May 2026_
