# HRMS Leave Accrual Period Boundary Cases

## Purpose
Specify how leave accrual behaves at calendar, employment, and policy boundaries.

## Policy decisions
Confirm accrual frequency, eligibility date, prorating rules, carry-forward limits, expiry rules, and treatment of unpaid leave. These rules should be configurable or clearly versioned rather than scattered across service methods.

## Boundary scenarios
- Employee joins on the first day of an accrual period.
- Employee joins on the final day of a period.
- Employee exits during an accrual period.
- Leap-year February.
- Month-end dates such as the 30th and 31st.
- Policy changes during an active year.
- Employee moves between leave categories.
- Accrual job executes twice for the same period.
- Accrual job runs late or is retried after a failure.

## Integrity requirements
Use an idempotent accrual key based on employee, leave type, and accrual period. Preserve ledger entries rather than silently overwriting balances. Calculate using the organization's configured timezone and policy calendar.

## Verification
Compare opening balance, accrual, usage, adjustment, expiry, and closing balance. Test concurrent accrual and leave approval to ensure the same balance is not consumed twice.

## Open questions
Document whether negative balances are permitted and who can approve manual adjustments.
