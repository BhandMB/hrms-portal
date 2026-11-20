# HRMS Audit Log Integrity and Access

Date: 2026-11-20

## Goals

Audit events should be attributable, access-controlled, useful for investigation, and protected from routine modification.

## Event content

Prefer structured fields: event type, actor identifier, target type and identifier, timestamp, outcome, correlation identifier, and a minimal change summary. Do not place passwords, session tokens, or full sensitive records in event payloads.

## Access model

- Permit audit viewing only to roles with an explicit operational or compliance need.
- Do not expose audit deletion or editing through ordinary application workflows.
- Restrict exports and record who generated them.
- Apply retention and archival policies approved by the organization.

## Reliability considerations

Define behavior when audit persistence fails. For security-sensitive actions, avoid reporting success if the required audit event cannot be durably recorded. Use a transactionally consistent strategy where appropriate.

## Verification

- [ ] Actor and target are captured for relevant events.
- [ ] Sensitive values are redacted.
- [ ] Unauthorized users cannot browse audit history.
- [ ] Event records cannot be edited through normal application APIs.
- [ ] Export activity is itself auditable.

## Scope

This is a design review checklist, not proof of an existing immutable audit system.