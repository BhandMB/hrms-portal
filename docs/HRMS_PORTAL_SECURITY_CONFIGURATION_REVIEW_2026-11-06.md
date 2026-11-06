# HRMS Portal Security Configuration Review

Date: 2026-11-06

## Configuration review
- Confirm production does not use sample or default credentials.
- Require strong, unique database credentials through environment configuration.
- Ensure secrets and local environment files are excluded from Git.
- Review CSRF protection, session handling, cookie flags, and logout behavior.
- Confirm password hashing uses an appropriate adaptive algorithm.
- Restrict actuator or diagnostic endpoints where present.
- Verify authorization on server-side routes and service methods.

## Data protection
Avoid logging employee personal data, payroll details, credentials, or tokens. Use least-privilege database access and encrypted transport in deployed environments.

## Verification
Test unauthenticated access, role escalation attempts, direct URL access, and session invalidation. This is a review checklist; inspect the live configuration before claiming controls are active.