# Payroll input snapshot and recalculation policy

## Principle
A payroll run must be reproducible from the inputs and rules approved at calculation time.

## Snapshot contents
Record payroll period, employee identifier, effective salary components, attendance/leave inputs, approved adjustments, rule/version identifiers, calculation timestamp, and initiating actor. Store only fields needed to explain the result.

## Lifecycle
- Draft: inputs may change; recalculation allowed.
- Reviewed: totals and exceptions are available for review.
- Approved: inputs and calculation version are frozen.
- Paid/closed: immutable outcome; corrections use adjustment or reversal.

## Recalculation rules
A material input change after review invalidates the prior calculation and requires new review. Changes after approval must not silently rewrite the run. Require authorization, reason, and audit event for reopening or adjustment.

## Controls
Use decimal arithmetic with documented currency scale and rounding. Reconcile employee totals to run totals. Restrict salary access and avoid logging payslip contents.

## Acceptance
Historical runs remain explainable from snapshots; identical versioned inputs reproduce identical results; post-approval changes are blocked or controlled.
