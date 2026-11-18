# HRMS Audit Log Integrity Review

## Events to capture
Capture security-relevant and business-critical events: authentication outcomes, role changes, employee record updates, leave decisions, attendance corrections, payroll transitions, and administrative configuration changes.

## Required attributes
Each event should include event ID, UTC timestamp, actor ID, action, target type and ID, outcome, correlation ID, and a minimal before/after summary where lawful and necessary.

## Integrity safeguards
- Application users must not be able to edit or delete audit records through normal application paths.
- Avoid storing passwords, tokens, full bank details, or unnecessary sensitive values.
- Restrict audit-log read access and record access to the logs themselves.
- Use append-only storage or a separately controlled sink for stronger tamper resistance.
- Define retention and deletion rules consistent with applicable obligations.

## Verification
Test successful and denied changes, missing actor context, correlation across requests, redaction, and restricted access to audit search. Document events intentionally not logged and why.