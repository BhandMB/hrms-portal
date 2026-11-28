# Payslip generation and download security

## Security requirements
Payslips contain confidential compensation data and must be protected as private employee documents.

## Access policy
- Authorize every download against the authenticated employee or explicitly permitted payroll role.
- Do not trust employee IDs or file paths supplied by the browser.
- Use opaque document identifiers and verify ownership before streaming content.
- Prevent public static hosting and directory traversal.
- Apply no-store cache headers and avoid sensitive filenames.

## Generation controls
Generate from an approved payroll snapshot and record its run/version reference. Ensure repeated generation is deterministic for the same approved inputs. Protect temporary files and clean them up according to policy.

## Tests
Attempt cross-employee downloads, expired sessions, guessed identifiers, path manipulation, role downgrade, and access after deactivation. Confirm audit events record access without storing payslip contents.
