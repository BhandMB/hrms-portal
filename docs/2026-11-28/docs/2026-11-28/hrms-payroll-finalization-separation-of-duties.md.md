# Payroll finalization separation of duties

## Objective
Prevent one person from preparing, approving, and finalizing the same payroll run when policy requires independent review.

## Role boundaries
Define permissions for preparation, review, approval, finalization, reopening, and adjustment. Enforce restrictions in the service/API layer, not only in page navigation.

## Workflow controls
- Record the preparer and immutable payroll-run version.
- Require an eligible reviewer who is not the preparer where separation is required.
- Revalidate totals and exceptions at approval time.
- Freeze approved inputs before finalization.
- Require elevated permission, reason, and audit event for reopening.
- Route post-payment corrections through adjustment or reversal.

## Tests
Verify self-approval denial, role changes during review, stale approval after recalculation, duplicate finalization, unauthorized reopen, and audit attribution. Ensure the UI and API return consistent outcomes.
