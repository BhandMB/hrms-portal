# HRMS Audit Log Retention and Access Policy

## Objective
Define safeguards for audit records covering employee, attendance, leave, payroll, and access-control events.

## Event content
Capture actor, action, target reference, timestamp, outcome, and a minimal change summary. Store before-and-after values only when justified by the audit purpose and privacy policy.

## Integrity
Application users should not be able to edit or delete audit records through normal business endpoints. Restrict database access and consider append-only storage or tamper-evident controls for higher assurance.

## Access
Limit audit search and export to authorized roles. Apply employee visibility restrictions where relevant. Log access to sensitive audit exports.

## Retention and disposal
Set retention periods based on applicable law, contracts, and organizational policy. Expire or archive records through a controlled process with evidence of completion. Do not invent a universal retention period.

## Privacy and security
Never record passwords, tokens, session cookies, or full payment credentials. Sanitize untrusted values to prevent log injection. Protect audit exports and backups.

## Verification
Test denied modification attempts, access by each role, timestamp consistency, retention job behavior, and audit coverage for sensitive workflows.
