# Payroll Rounding and Currency Rules

- Store monetary values using decimal types, never binary floating-point.
- Define currency and scale for each payroll component.
- Specify whether rounding occurs per earning line or only at the final payable total.
- Keep gross, deductions, and net-pay calculations reproducible.

## Test cases
Include half-cent boundaries, negative adjustments, zero values, large amounts, and repeated recalculation. Verify displayed totals reconcile with persisted totals.

## Auditability
Persist calculation inputs and rule/version identifiers needed to explain a payslip. Do not silently recalculate historical payroll using newly changed rules.