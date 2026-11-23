# HRMS Leave Balance Accrual Reconciliation

Date: 2026-11-23

## Objective
Make leave accrual calculations explainable and reconcilable over time.

## Policy inputs
Document leave category, eligibility date, accrual frequency, accrual rate, carry-forward cap, rounding rule, and effective policy version. Policy changes should not silently rewrite previously earned balances.

## Reconciliation model
For each employee and leave category, reconcile opening balance plus accruals and adjustments minus approved usage and expiry with the closing balance. Keep each movement traceable to its source event.

## Edge cases
Consider joiners and leavers mid-period, unpaid leave, policy changes, leap years, negative balance rules, and corrections after payroll close.

## Verification
- [ ] Calculation uses the correct policy version.
- [ ] Repeated accrual processing is idempotent.
- [ ] Adjustments require authorization and reason.
- [ ] Balance history explains the current total.
- [ ] Concurrent requests cannot double-apply accrual.

## Scope
Reconciliation design only; no employee balances were recalculated.