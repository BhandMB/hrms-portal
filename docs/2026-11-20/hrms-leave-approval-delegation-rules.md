# HRMS Leave Approval: Delegation Rules

Date: 2026-11-20

## Purpose

Specify safe behavior when an assigned leave approver is unavailable.

## Rules to define

- Delegation must have an explicit start and end date.
- Only an authorized manager or administrator may assign a delegate.
- A delegate must have the required approval permission and organizational scope.
- The original approver remains visible in the audit history.
- Delegation must not allow a requester to approve their own leave.
- Expired or revoked delegation must stop authorizing new decisions immediately.

## Decision audit

For every approval or rejection, record the request ID, acting user, original approver, decision, timestamp, and delegation reference where applicable. Avoid storing unnecessary medical or personal details in the audit event.

## Test scenarios

- [ ] Valid delegate can act during the active window.
- [ ] Delegate cannot act before or after the window.
- [ ] Self-approval is denied.
- [ ] Revocation takes effect for subsequent requests.
- [ ] Audit data identifies both actor and delegated authority.

## Scope

This document describes expected policy and does not claim delegation has been implemented.