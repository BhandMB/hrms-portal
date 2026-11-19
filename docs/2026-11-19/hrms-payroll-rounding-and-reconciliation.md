# Payroll Rounding and Reconciliation

## Objective
Make payroll calculations deterministic and explainable at currency precision.

## Calculation rules
- Use decimal arithmetic for monetary values.
- Define currency scale and rounding mode centrally.
- Specify whether rounding occurs per component, per employee, or only at the final payable amount.
- Keep unrounded intermediate values where audit and recalculation requirements need them.
- Version calculation rules so historical payroll can be reproduced.

## Reconciliation
For each payroll run, compare component totals with employee-level totals and the final payable total. Record adjustments explicitly rather than silently changing calculated values.

## Test scenarios
Include half-way rounding boundaries, negative adjustments, zero-value components, multiple allowances, deductions, and recalculation of the same frozen input snapshot.

## Evidence
Document the currency and rounding policy, representative calculation examples, and automated test results once executed.