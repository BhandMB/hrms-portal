# HRMS Portal Deployment Runbook

Date: 2026-11-06

## Before deployment
- Confirm the release commit and build artifact.
- Review configuration differences between local and production profiles.
- Set database credentials and application secrets outside source control.
- Verify database backup and migration/rollback procedures.
- Confirm demo accounts and sample data are disabled or replaced.

## Deployment
1. Deploy to a staging environment first.
2. Verify database connectivity and schema state.
3. Start the application and inspect startup logs.
4. Run authentication, employee read/write, and role-access smoke tests.
5. Promote the same verified artifact to production.

## Rollback
Record the previous artifact and compatible database state. Roll back application code only when schema compatibility is understood. Never restore production data without an approved recovery decision.

## Post-deployment
Check health, error rate, and critical workflows. Record release ID, operator, timestamps, test evidence, and incidents. Adapt commands to the actual hosting platform; this runbook does not claim a deployment has occurred.