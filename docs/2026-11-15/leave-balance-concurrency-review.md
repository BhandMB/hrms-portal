# Leave Balance Concurrency Review

Two simultaneous leave requests can both read the same available balance and over-allocate leave if updates are not coordinated.

## Review options
- Use a database transaction with locking or optimistic versioning.
- Re-check available balance inside the transaction that records the request.
- Make approval and cancellation transitions idempotent.
- Prevent duplicate deductions when a request is retried.

## Test design
Run concurrent requests against a balance that can cover only one request. Assert the final balance and approved days are consistent and one request is rejected or remains pending according to policy.

Capture transaction outcomes without logging private leave details unnecessarily.