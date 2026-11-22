# HRMS Role Permission Regression Matrix

Date: 2026-11-22

## Purpose
Maintain a repeatable permission review for ADMIN, HR, and EMPLOYEE workflows.

## Matrix dimensions
For each endpoint and UI action, record the required role, organizational scope, ownership rule, and expected response for unauthenticated, unauthorized, and permitted callers.

## High-value cases
- ADMIN can perform explicitly administrative operations.
- HR can manage only the records within its granted scope.
- EMPLOYEE can view and update only permitted self-service fields.
- Changing an identifier in a URL cannot bypass ownership checks.
- Hidden buttons do not replace backend authorization.
- Exports, reports, and nested resources enforce equivalent rules.

## Test evidence
Use integration tests against secured endpoints, not only controller unit tests. Include both positive and negative cases and verify that denied responses do not leak record contents.

## Checklist
- [ ] Every sensitive endpoint has an explicit permission rule.
- [ ] Role and ownership checks are tested.
- [ ] UI visibility aligns with server permissions.
- [ ] Permission changes have regression coverage.

## Scope
Matrix template only; it does not assert that the current application passes these cases.