# Production Secrets Rotation Runbook

## Scope
Rotate database credentials, signing keys, integration tokens, and other application secrets without exposing values in source control or logs.

## Preparation
- Inventory secret consumers and owners.
- Confirm a secure replacement value is generated and stored in the approved secret manager.
- Identify whether the system supports overlapping old and new credentials.
- Schedule the change and define a rollback or recovery path.

## Execution
1. Deploy the new secret to the intended runtime configuration.
2. Restart or refresh consumers according to the platform's supported mechanism.
3. Verify database and integration connectivity.
4. Revoke the old credential after confirming successful adoption.
5. Monitor authentication failures and application health.

## Safety
Never paste secret values into tickets, chat, commit messages, or diagnostic logs. Restrict access to the runbook and record timestamps and responsible operators, not secret contents.

## Completion evidence
Record secret identifier, affected services, rotation time, verification outcome, and any follow-up required.