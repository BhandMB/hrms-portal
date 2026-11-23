# HRMS Payroll Input Snapshot and Versioning

Date: 2026-11-23

## Purpose
Preserve the exact inputs and policy versions used to produce a payroll result.

## Snapshot contents
Identify the payroll period, employee reference, approved attendance version, leave adjustments, compensation components, applicable policy versions, calculation build/version, and generation timestamp. Store only data required for audit and reproducibility.

## Change handling
Changes to source records after snapshot creation should be recorded as later versions. Define whether a payroll run must be recalculated, explicitly reopened, or left unchanged pending an approved adjustment.

## Reproducibility
A reviewer should be able to identify the inputs and rules that produced a payslip total without relying on mutable current records.

## Acceptance checks
- [ ] Snapshot references immutable or versioned inputs.
- [ ] Re-running a calculation is traceable.
- [ ] Post-snapshot changes are visible.
- [ ] Sensitive snapshot access is restricted.
- [ ] Retention follows approved policy.

## Scope
Design guidance only; no payroll run or snapshot was created.