# HRMS Leave Approval State Machine

## States
Document the supported states for a leave request, such as submitted, approved, rejected, cancelled, and withdrawn. Define permitted transitions explicitly rather than allowing arbitrary status updates.

## Transition controls
- Only authorized roles may approve or reject a request.
- A request cannot be approved after cancellation.
- Repeated approval commands must not deduct leave twice.
- Every transition records actor, timestamp, and a safe reason where required.

## Tests
Exercise every permitted transition, each forbidden transition, duplicate commands, and concurrent approval/cancellation attempts.