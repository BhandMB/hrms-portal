# Leave balance ledger reconciliation

## Purpose
Make leave balances explainable through recorded movements rather than relying only on a mutable total.

## Ledger model
Each movement includes employee, leave type, effective date, quantity, movement type (accrual, booking, cancellation, adjustment, expiry), source reference, actor/system identity, and timestamp. Corrections use compensating entries, not edits to history.

## Balance calculation
Define opening balance, accrual, approved usage, cancellations, adjustments, carry-forward, and expiry in a documented order. Apply policy effective dates and precision consistently. Show pending requests separately unless policy explicitly reserves them.

## Reconciliation checks
- Ledger-derived balance equals displayed balance.
- Cancellation reverses original booking exactly once.
- Replaying an accrual job does not duplicate entries.
- Negative balances follow explicit policy and authorization.
- Policy changes use effective dates rather than rewriting history.

## Safeguards
Run scheduled reconciliation and report mismatches. Restrict adjustments and require reasons. Never silently overwrite a mismatch; preserve evidence and resolve the underlying ledger issue.
