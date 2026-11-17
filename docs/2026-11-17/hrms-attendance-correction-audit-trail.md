# HRMS Attendance Correction Audit Trail

## Required history
For each correction, capture the attendance date, affected employee reference, previous value, corrected value, actor, timestamp, and reason where appropriate.

## Workflow controls
- Restrict corrections to authorized roles and permitted periods.
- Require review for corrections that affect payroll inputs.
- Preserve the original event rather than overwriting history without trace.
- Make repeated correction requests idempotent or detect duplicates.

## Verification
Test authorized and unauthorized corrections, corrections after payroll close, repeated requests, and review rejection. Confirm the displayed attendance state can be reconstructed from the audit history.