# Payroll Input Freeze Policy

## Purpose
Prevent late changes to attendance, leave, employee compensation, or bank details from silently changing a payroll run already under review.

## Proposed lifecycle
- DRAFT: inputs remain editable by authorized users.
- FROZEN: payroll input snapshot is captured for a defined period.
- REVIEW: calculated results are checked and exceptions resolved.
- APPROVED: an authorized approver signs off the run.
- CLOSED: the run is immutable; corrections use an adjustment workflow.

## Controls
- Store the cutoff timestamp, period, input snapshot identifier, and initiating actor.
- Show late-arriving changes as exceptions; do not silently include them.
- Require elevated permission and a reason to reopen a frozen run.
- Preserve the original calculation inputs and approval history.
- Keep recalculation deterministic for the same snapshot and ruleset.

## Acceptance checks
Verify that edits after freeze do not alter the snapshot, unauthorized users cannot reopen it, approvals are attributable, and post-close corrections create traceable adjustments.