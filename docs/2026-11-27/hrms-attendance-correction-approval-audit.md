# Attendance correction approval and audit controls

## Workflow
1. Employee or authorized HR user submits date, requested values, and reason.
2. System validates the date and prevents duplicate active requests.
3. An authorized reviewer approves or rejects; requester cannot self-approve where separation of duties applies.
4. Approved corrections update attendance and create an audit event.
5. Requester can see the decision and a safe explanation.

## Integrity
Retain original and corrected values, effective date, actor, reviewer, decision time, and reason. Use a transaction for approval, update, and audit event. Prevent concurrent duplicate approvals.

## Controls
Limit correction windows by policy. Require elevated authorization for older or payroll-closed periods. A correction affecting finalized payroll must use controlled recalculation/adjustment, not silently alter paid results.

## Tests
Self-approval denial, unauthorized department access, duplicate approval, stale request state, invalid dates, payroll-closed period, and audit persistence.
