# HRMS Payroll Period Close and Reopen Controls

## Objective
Protect payroll periods from accidental changes after review and close.

## Period lifecycle
Define explicit Draft, Calculated, Reviewed, Approved, and Closed states if they match the product's workflow. Closing a period should freeze the approved calculation revision and prevent ordinary edits to its inputs or outputs.

## Reopen controls
Require a privileged role, reason, and audit event to reopen a period. Prefer creating a new revision rather than overwriting a previously approved result. Define whether downstream payslips or reports must be regenerated.

## Consistency
Ensure employee-level totals reconcile with the payroll register. Prevent attendance or leave corrections from silently changing a closed period. Define how late corrections are handled in a subsequent adjustment.

## Test scenarios
- Close an approved period.
- Attempt to edit a closed payroll record.
- Reopen with and without required permission.
- Reopen with a missing reason.
- Duplicate close or reopen request.
- Late attendance correction after close.
- Reconciliation between original and revised totals.

## Evidence
Store period identifier, revision, approver, close timestamp, reopen reason, and references to the input snapshot.
