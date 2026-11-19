# Attendance Correction Approval

## Purpose
Create an auditable process for correcting attendance records after initial submission.

## Workflow
- Employee or authorized HR user submits the requested correction with date, reason, and supporting context.
- The system validates the employee, date range, and applicable attendance policy.
- An authorized reviewer approves or rejects the request.
- Approval updates the attendance record and writes an audit event linked to the request.
- The requester can view the decision and its timestamp.

## Controls
- A requester must not approve their own correction unless policy explicitly allows it.
- Preserve original values and corrected values in the audit history.
- Prevent duplicate pending requests for the same employee and attendance date.
- Recheck authorization and record version when approval is applied.
- Define how corrections affect payroll periods that are already frozen or closed.

## Tests
Cover self-approval, duplicate requests, out-of-period dates, concurrent edits, rejected requests, and corrections after payroll freeze.