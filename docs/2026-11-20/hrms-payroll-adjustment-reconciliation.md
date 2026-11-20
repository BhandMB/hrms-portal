# HRMS Payroll Adjustment Reconciliation

Date: 2026-11-20

## Objective

Make payroll adjustments traceable from request through approval and payroll-period reconciliation.

## Required adjustment details

Capture employee reference, payroll period, adjustment category, amount, currency, reason code, supporting reference, requester, approver, and timestamps. Apply field-level access controls to compensation information.

## Workflow controls

1. Validate that the payroll period is open.
2. Reject unsupported currency and invalid precision.
3. Require approval by a permitted user other than the requester.
4. Prevent duplicate application of the same adjustment.
5. Lock or snapshot approved inputs when payroll is finalized.
6. Record reversals as new linked entries rather than silently overwriting history.

## Reconciliation checks

Compare approved adjustments with the payroll calculation output and the final payslip totals. Investigate missing, duplicated, or changed entries before closing the period.

## Acceptance checks

- [ ] Every adjustment has a traceable source.
- [ ] Approval separation is enforced server-side.
- [ ] Finalized periods cannot be silently edited.
- [ ] Reversals preserve the original audit trail.
- [ ] Reconciliation exceptions are reviewable.

## Scope

Process documentation only; no payroll calculation has been run or verified.