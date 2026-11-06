# HRMS Portal Backup and Restore Drill

Date: 2026-11-06

## Objective
Prove that a recoverable backup can be restored into an isolated environment and that essential HRMS workflows still function.

## Drill sequence
1. Identify the database, uploaded files, and configuration that require backup.
2. Create a timestamped backup using the approved database tooling.
3. Record backup size, checksum, duration, and retention location.
4. Restore into an isolated database; never overwrite production during a drill.
5. Verify schema, representative employee records, and referential integrity.
6. Run login, employee lookup, leave, attendance, and payroll smoke tests where those modules are enabled.
7. Record recovery time and any data-loss window.

## Governance
Restrict backup access, encrypt backups at rest, and define retention and deletion rules. Keep secrets separate from the backup artifact.

Document actual results, not assumptions. This file defines a drill procedure and does not state that a restore has been performed.