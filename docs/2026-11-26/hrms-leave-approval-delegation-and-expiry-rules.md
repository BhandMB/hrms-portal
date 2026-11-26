# HRMS Leave Approval Delegation and Expiry Rules

## Purpose
Define how pending leave approvals are handled when the assigned approver is unavailable.

## Policy decisions
Specify who may delegate approval, whether delegation is time-bounded, which leave categories are covered, and whether a delegated approver may further delegate. Avoid implicit delegation based only on department membership.

## Workflow rules
- Preserve the original approver and the delegated actor in the decision history.
- Validate that the delegate is active and authorized for the employee's scope.
- Expire delegation at the configured end date.
- Define reassignment behavior for pending requests when an approver is deactivated.
- Prevent the requester from approving their own leave through delegation.

## Security
Check authorization on the server for every approval action. Hiding an approval control in the UI is not sufficient. Prevent users from changing delegation targets through request tampering.

## Test cases
- Valid delegation during its active window.
- Attempted approval after delegation expiry.
- Delegate outside the employee's approval scope.
- Approver deactivated with pending requests.
- Circular or repeated delegation.
- Self-approval through a delegated path.

## Audit
Record delegation creator, delegate, scope, effective period, reason, and every decision made under the delegation.
