# HRMS Attendance Correction Evidence Requirements

## Scope
Define the evidence and audit trail required when attendance entries are corrected.

## Required correction data
- Employee and attendance date
- Original value and corrected value
- Correction category
- Reason supplied by requester
- Requester and submission timestamp
- Reviewer decision and decision timestamp
- Supporting reference, where policy requires it

## Workflow
Employees may request corrections for their own attendance. A designated manager or HR reviewer approves or rejects the request. Direct edits to approved historical entries should be restricted to a controlled administrative process.

## Integrity rules
A correction must not overwrite the original evidence. Store the before-and-after values and link the correction to the affected attendance record. Apply approved changes and derived attendance totals in one transaction where possible.

## Privacy
Limit access to supporting documents. Avoid putting sensitive explanations or attachments into general application logs. Define retention based on organizational policy.

## Test checklist
- Employee cannot correct another employee's attendance.
- Pending correction does not alter payroll inputs prematurely.
- Rejected correction leaves the original entry intact.
- Approved correction updates the intended date only.
- Repeated approval does not apply the correction twice.
- Audit history remains available after later corrections.

## Operational metric
Monitor pending correction age so unresolved requests do not remain unnoticed through payroll close.
