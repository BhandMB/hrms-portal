# HRMS Payroll Period Lock and Reopen

Date: 2026-11-22

## Purpose
Specify how payroll periods are finalized and how exceptional corrections are controlled.

## Lock behavior
Before locking, validate required employee inputs, approved attendance, leave adjustments, and pending payroll changes. Record the period identifier, calculation version, totals, actor, and timestamp. A locked period must not accept ordinary edits.

## Reopen controls
Require an elevated permission, a reason, and an audit event. Reopening should identify affected employees and downstream payslips or reports. Recalculation must be explicit and traceable; avoid silently replacing previously issued results.

## Reconciliation
Compare the finalized output with the approved inputs and preserve a snapshot or equivalent evidence of the calculation basis.

## Acceptance checks
- [ ] Only authorized roles can lock or reopen.
- [ ] Lock operation is repeat-safe.
- [ ] Reopen requires a reason and audit trail.
- [ ] Changes after reopening are identifiable.
- [ ] Finalized output can be reconciled to its inputs.

## Scope
Operational design guidance; no payroll period was locked or reopened.