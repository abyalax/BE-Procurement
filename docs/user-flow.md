Berikut versi updated compact planning docs untuk user flow Procurement Apps.

---

# Procurement Apps — User Flow Planning

## Core Procurement Lifecycle

```text id="7zcore"
Purchase Requisition
→ Approval Workflow
→ RFQ
→ Vendor Response
→ Evaluation
→ Vendor Selection
→ Purchase Order
→ Goods Receipt
→ Invoice
→ Payment Tracking
```

---

# Core Roles

## Internal Roles

| Role               | Main Responsibility        |
| ------------------ | -------------------------- |
| Employee           | Create procurement request |
| Supervisor         | Initial approval           |
| Manager            | Department/budget approval |
| Director           | Strategic approval         |
| Purchasing Staff   | Procurement operation      |
| Purchasing Manager | Procurement governance     |
| Warehouse Staff    | Goods receiving            |
| Finance Staff      | Invoice verification       |
| Finance Manager    | Financial approval         |
| Admin              | System management          |

---

## External Roles

| Role   | Main Responsibility      |
| ------ | ------------------------ |
| Vendor | RFQ response & quotation |

---

# Core Business Rules

## Segregation of Duties

Requester cannot:

* approve own PR
* select own vendor
* approve own procurement

Finance cannot:

* create PO

Vendor cannot:

* see competitor quotation

---

# Workflow Architecture

System uses 3 tracking layers:

| Layer             | Purpose                    |
| ----------------- | -------------------------- |
| Status            | Current business condition |
| Stage             | Current business phase     |
| Timeline Activity | Historical workflow events |

---

# Status Design

Status is:

* lightweight
* summarized
* dashboard-oriented

Example RFQ Status:

```text id="4mstatus"
Draft
Published
Active
Evaluating
Awarded
Failed
Cancelled
```

Status should NOT contain:

* approval step detail
* vendor detail
* workflow branching logic

---

# Stage Design

Stage represents major procurement phase.

Example RFQ Stages:

```text id="2mstage"
RFQ Preparation
Vendor Response
Evaluation
Approval
Awarding
PO Processing
```

Only 1 active stage at a time.

---

# Timeline Activity Design

Timeline stores historical business events.

Each activity contains:

* activity type
* actor
* timestamp
* notes
* metadata

Timeline is append-only.

No edit/delete.

---

# RFQ Timeline Example

## RFQ Preparation

```text id="1mrfq"
RFQ Created
Vendor Invitations Sent
```

## Vendor Response

```text id="2mvendor"
Vendor A Accepted
Vendor B Declined
Quotation Submitted
Quotation Revised
```

## Evaluation

```text id="3meval"
Technical Evaluation Started
Commercial Evaluation Started
Negotiation Started
Vendor Recommended
```

## Approval

```text id="4mapproval"
Manager Approved
Finance Approved
Director Approved
```

---

# Approval Workflow Concept

Approval is separated from document status.

Bad approach:

```text id="5mbad"
WAITING_MANAGER_APPROVAL
WAITING_DIRECTOR_APPROVAL
```

Recommended approach:

* RFQ status remains stable
* approval handled by dedicated workflow

Example:

```text id="6mgood"
RFQ Status = Evaluating

Approval Flow:
- Purchasing Manager → Approved
- Finance Manager → Pending
- Director → Waiting
```

---

# Main User Flows

## Employee Flow

```text id="7memp"
Login
→ Create PR
→ Submit PR
→ Monitor Approval
→ View Procurement Result
```

---

## Approval Flow

```text id="8mappr"
PR Submitted
→ Supervisor Approval
→ Manager Approval
→ Director Approval (conditional)
→ Approved
```

Conditional approval:

```text id="9mcond"
< 5 juta → Supervisor
5-20 juta → Manager
> 20 juta → Director
```

---

## Purchasing Flow

```text id="10mpurch"
Receive Approved PR
→ Create RFQ
→ Invite Vendors
→ Monitor Vendor Responses
→ Evaluate Quotations
→ Select Vendor
→ Create PO
```

---

## Vendor Flow

```text id="11mvendor"
Vendor Login
→ View RFQ
→ Accept/Reject Invitation
→ Submit Quotation
→ Revise Quotation
→ Receive PO
→ Upload Invoice
```

---

# Exception Flow

## RFQ Failure Handling

Conditions:

* all vendors decline
* no quotation submitted
* RFQ expired

Flow:

```text id="12mmanual"
RFQ Failed
→ Manual Sourcing Stage
→ Buyer Assignment
→ Offline Negotiation
→ Vendor Selection
```

---

# Audit & Tracking

All business actions recorded:

* approvals
* rejection
* quotation revision
* negotiation
* PO creation
* invoice verification

Audit history is immutable.

---

# Planning Principles

## Do

* keep status minimal
* separate workflow from status
* use grouped timeline activities
* treat procurement as workflow system

## Avoid

* giant status enums
* mixing approval into status
* single flat timeline
* encoding every business event as status

---

# Final Conceptual Model

```text id="13mfinal"
Procurement Document
├── Current Status
├── Current Stage
├── Approval Workflow
└── Timeline Activities
```

_Last Updated on 17 May 2026_