# HRMS Attendance Timesheet Approval Controls

Date: 2026-11-22

## Objective
Define controls for reviewing and approving attendance records before they feed payroll or reporting.

## Workflow
Employees submit or correct their timesheet; an authorized reviewer validates exceptions; approved records become locked for the relevant payroll cut-off. Reopening a finalized period requires a separately authorized correction workflow.

## Controls
- Prevent employees from approving their own entries.
- Record original and corrected values with actor, timestamp, and reason.
- Define rules for overlapping shifts, missing punches, holidays, and overnight work.
- Use a consistent timezone and daylight-saving policy where applicable.
- Keep approval status separate from payroll calculation status.

## Verification
- [ ] Self-approval is denied.
- [ ] Corrections retain an audit trail.
- [ ] Duplicate submissions are handled safely.
- [ ] Locked periods reject ordinary edits.
- [ ] Approved attendance changes are traceable into payroll.

## Scope
Workflow specification only; no attendance records were processed.