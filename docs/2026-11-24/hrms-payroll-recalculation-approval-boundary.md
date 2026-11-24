# HRMS Payroll Recalculation and Approval Boundary

## Objective
Prevent recalculation from silently changing payroll that has already been reviewed or finalized.

## Proposed lifecycle
1. Draft payroll is calculated from a defined input snapshot.
2. Reviewer inspects totals, exceptions, and employee-level changes.
3. Approval freezes the calculation version.
4. A correction after approval creates a new revision with a reason.
5. Finalized payroll is immutable through ordinary edit endpoints.

## Input snapshot
Persist or otherwise reproducibly identify the salary, attendance, leave, deduction, and policy inputs used for each calculation. Record the calculation version and effective payroll period.

## Recalculation rules
- Draft runs may be recalculated.
- Approved runs require an authorized reopen or revision workflow.
- Finalized runs require a correction process rather than in-place mutation.
- Repeating the same calculation with the same inputs should produce the same result, subject to documented rounding rules.

## Authorization
Separate permission to calculate, review, approve, reopen, and finalize. A user should not approve their own calculation if separation of duties is required by the organization's policy.

## Verification
Test changed salary inputs, corrected attendance, duplicate calculation requests, stale approval attempts, and unauthorized reopen attempts. Compare employee totals and payroll grand totals after every revision.

## Audit
Store who initiated and approved each revision, why it changed, and which prior revision it supersedes.
