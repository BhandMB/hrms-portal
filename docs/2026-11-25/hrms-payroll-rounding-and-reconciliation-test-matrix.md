# HRMS Payroll Rounding and Reconciliation Test Matrix

## Objective
Make payroll calculations reproducible and prevent rounding differences between employee statements and payroll totals.

## Rules to document
- Currency and decimal precision
- Rounding mode for each calculation stage
- Whether rounding occurs per component or only at the final total
- Treatment of negative adjustments and reversals
- Tax and deduction precision
- Handling of zero-value and very small amounts

## Test matrix
1. Halfway decimal values for each rounding mode.
2. Multiple components whose individually rounded sum differs from a rounded aggregate.
3. Negative adjustment and reversal.
4. Zero salary component.
5. Large values near configured limits.
6. Recalculation with identical input snapshot.
7. Employee payslip total compared with payroll register total.
8. Payroll period totals compared with the sum of employee records.

## Implementation guidance
Use decimal arithmetic rather than binary floating point for currency. Keep rounding rules centralized and versioned. Avoid duplicating formula logic in the UI.

## Reconciliation evidence
Retain calculation version, source input snapshot reference, component totals, final net amount, and any manual adjustment approval. Never log unnecessary personal or bank details.

## Acceptance criteria
The same input snapshot and calculation version produce the same result, and all aggregate totals reconcile within the explicitly defined currency precision.
