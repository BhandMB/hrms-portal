# Leave policy versioning and effective dates

## Goal
Make historical leave decisions reproducible when accrual, carry-forward, or eligibility rules change.

## Version model
Each policy version should include a unique identifier, effective-from/to dates, leave type, eligibility rules, accrual method, carry-forward limits, expiry rules, and approval metadata.

## Application rules
- Select the policy version effective on the relevant business date.
- Do not retroactively recalculate historical balances merely because a new policy is published.
- Define transition handling for employees whose leave year spans two versions.
- Reject overlapping effective periods for the same policy scope unless explicitly supported.
- Record manual exceptions as separately authorized adjustments.

## Verification
Test policy boundary dates, leap years, mid-year changes, employee eligibility changes, future policy publication, and historical report reproduction. Retain the policy version reference on each generated ledger movement.
