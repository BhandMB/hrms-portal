# HRMS Leave Request Cancellation State Rules

## Purpose
Specify cancellation behavior so leave balances and approval history remain consistent.

## State model
A leave request may move through Draft, Submitted, Approved, Rejected, and Cancelled. The exact supported states must match the application implementation; this document is a target contract, not evidence that every state currently exists.

## Transition rules
- Draft may be cancelled by its owner without an approval action.
- Submitted may be withdrawn by its owner while pending, subject to policy.
- Approved leave may require HR approval to cancel, especially after the leave period begins.
- Rejected requests remain historical records; cancellation should not erase them.
- Cancelled requests are terminal unless a new request is created.

## Balance integrity
Reserve or deduct leave balance at one defined transition, and reverse it exactly once on an approved cancellation. Use a transaction to update request state and balance together. Repeated cancellation requests must be idempotent.

## Audit requirements
Retain requester, approver where applicable, cancellation reason, previous state, new state, and timestamps. Never delete the original approval decision.

## Test cases
- Owner withdraws a pending request.
- Employee attempts to cancel another person's request.
- HR cancels an approved future request.
- Cancellation is submitted twice.
- Cancellation races with approval.
- Balance is restored exactly once.
- A cancellation after leave has started follows the configured policy.

## Open decisions
Confirm whether partial-day cancellation and retroactive cancellation are supported before implementing the rules.
