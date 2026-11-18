# Leave Balance Ledger Design

## Goal
Make leave balances explainable and auditable instead of storing only a mutable total.

## Suggested model
Represent each balance change as an immutable transaction with employee, leave type, effective date, quantity, reason, source request, actor, and creation timestamp. Use explicit transaction types such as accrual, approved leave debit, cancellation reversal, and authorized adjustment.

## Invariants
- A request cannot debit a balance before approval.
- A cancellation reverses the original debit exactly once.
- Adjustments require a reason and an authorized actor.
- Repeated processing of the same source event must not create duplicate ledger entries.
- Balance views are derived from posted entries or reconciled against them.

## Workflow
1. Validate policy and available balance.
2. Approve the request inside a transaction.
3. Post a uniquely identified debit entry.
4. Recalculate or update the balance projection.
5. Record the resulting audit event.

## Tests
Exercise duplicate approval, concurrent requests, cancellation after debit, adjustment permissions, and reconciliation between ledger totals and displayed balances.