# HRMS Deployment Rollback Runbook

## Before deployment
- Confirm artifact version, configuration source, and database backup.
- Review schema migrations for backward compatibility.
- Verify health checks, secrets, and external dependencies.
- Record the currently deployed version.

## Rollback procedure
1. Stop promotion of the faulty release.
2. Restore the previous application artifact and known-good configuration.
3. Apply database recovery only through an approved, tested migration plan.
4. Run smoke tests for login, role access, employee search, and core workflows.
5. Communicate status and preserve incident evidence.

An application rollback does not automatically reverse database changes.