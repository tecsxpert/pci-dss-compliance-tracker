# Demo Script — Java Developer 2
## Day 17 Rehearsal Version | PCI-DSS Compliance Tracker

**Your section covers:** Audit log, email notification, search/filter, status update, CSV export
**Estimated time:** ~90 seconds
**Prerequisite:** Java Developer 1 has just created a record and shown the AI response.

---

## STEP 1 — Show Audit Log

**[ACTION]:** Navigate to the audit log view for the record just created by Java Developer 1.
Open: `http://localhost/audit-log` or click "Audit History" on the record detail page.

**[SAY]:**
> "Every create, update, and delete is automatically captured in our audit log.
> You can see who changed what, when, and what the data looked like before and after.
> This is implemented using Spring AOP — no developer has to remember to log anything."

**[EXPECTED]:**
- Audit log table shows at least one `CREATE` entry
- Columns visible: `Action`, `Changed By`, `Changed At`, `Entity Type`, `Old Value` (null), `New Value` (JSON populated)

---

## STEP 2 — Trigger Email Notification

**[ACTION]:** Via Swagger UI (`http://localhost:8080/swagger-ui/index.html`) or frontend,
update the record status from `IN_PROGRESS` to `NON_COMPLIANT` using:
`PUT /api/v1/compliance-records/{id}`

**[SAY]:**
> "When a record status changes, our scheduler and event system sends an HTML email
> notification to the assigned team member automatically.
> The email is built using Thymeleaf templates — let me show you the mail logs."

**[EXPECTED]:**
- Record saves successfully (HTTP 200)
- Backend logs show: `Sending compliance update email to...`
- OR show Mailhog at `http://localhost:8025` with the email in inbox

---

## STEP 3 — Search and Filter

**[ACTION]:** Navigate to the compliance records list page.
Type a keyword (e.g. `firewall`) into the search bar.
Then select `NON_COMPLIANT` from the status filter dropdown.

**[SAY]:**
> "The search is debounced at 300ms to avoid hammering the API.
> Results are paginated and sortable by any column.
> The URL query parameters update in real time — this makes the view bookmarkable."

**[EXPECTED]:**
- Filtered results appear with matching records highlighted
- URL updates with `?q=firewall&status=NON_COMPLIANT`
- Pagination bar shows correct page count

---

## STEP 4 — Status Update Live

**[ACTION]:** Open a record's edit form. Change `compliance_score` from current value to `45`.
Change status to `NON_COMPLIANT`. Click Save.
Then immediately open the audit log for that record.

**[SAY]:**
> "The update is reflected immediately. The audit log captures old and new values,
> and Redis cache is evicted so the next read is always fresh — no stale data."

**[EXPECTED]:**
- Record shows updated score and status
- Status badge changes colour (red for NON_COMPLIANT)
- Audit log shows new `UPDATE` entry with `oldValue` containing previous JSON and `newValue` containing updated JSON

---

## STEP 5 — CSV Export

**[ACTION]:** Ensure you are logged in as ADMIN. Click the `Export CSV` button on the records list page.

**[SAY]:**
> "VIEWER role users cannot export data — only ADMIN and MANAGER can.
> This limits bulk data exfiltration risk, which is a key PCI-DSS requirement.
> The filename includes today's date automatically."

**[EXPECTED]:**
- Browser downloads a `.csv` file
- Filename format: `compliance_records_YYYY-MM-DD.csv`
- File opens correctly in Excel with proper UTF-8 encoding (no garbled characters)

---

## TRANSITION OUT

**[SAY]:**
> "I will hand over to [Java Developer 3] to show the dashboard and analytics."

---

## Rehearsal Notes

- Time your section: target 90 seconds
- Do NOT read from this script during the actual demo
- If any step fails: calmly say "let me show this from another angle" and use backup screenshots
