# HRMS Department Transfer Effective Dating

Date: 2026-11-23

## Objective
Define how department transfers affect employee history and downstream workflows.

## Transfer record
Capture employee reference, source and destination department, effective date, requester, approver, reason code, and creation timestamp. Preserve historical department assignments rather than overwriting the only record.

## Business rules
Specify whether the transfer date is inclusive, how pending leave approvals are reassigned, which manager gains approval scope, and how reporting periods attribute employee activity. Do not allow a user to approve their own transfer if separation of duties is required.

## Data consistency
Ensure employee profile, authorization scope, reporting, and payroll use a consistent effective-dated assignment. Define behavior for future-dated and backdated transfers.

## Verification
- [ ] Effective date boundaries are tested.
- [ ] Historical assignments remain queryable.
- [ ] Manager permissions change at the intended time.
- [ ] Pending workflows have defined ownership.
- [ ] Backdated corrections are auditable.

## Scope
Workflow specification only; no transfer records were changed.