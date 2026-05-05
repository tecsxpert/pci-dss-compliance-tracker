# Demo Script — Java Developer 2
## Day 20 Demo Day FINAL Version | PCI-DSS Compliance Tracker
## Date: Friday 9 May 2026

**Section time allocation:** ~90 seconds
**Transition in:** Java Developer 1 hands over after creating a record and showing AI response.

---

## [TRANSITION IN]

**[SAY]:**
> "Let me show you what happens behind the scenes every time a record is touched."

---

## BEAT 1 — Audit Log (CREATE entry)

**[ACTION]:** Navigate to the record just created by Java Developer 1. Click "Audit History".

**[SAY]:**
> "Every create, update, and delete is captured automatically using Spring AOP.
> You can see the action type, who made the change, the timestamp,
> and a full JSON snapshot of the data before and after."

**[EXPECTED]:** Audit log table shows `CREATE` entry with `changedBy`, `changedAt`, `entityType`, `oldValue: null`, `newValue: {…}`

---

## BEAT 2 — Live Status Update

**[ACTION]:** Click Edit on the record. Change status from `IN_PROGRESS` to `NON_COMPLIANT`. Click Save.

**[SAY]:**
> "I will update the status now. Watch the audit log."

**[EXPECTED]:** Record saves. Status badge changes to red `NON_COMPLIANT`.

---

## BEAT 3 — Audit Log (UPDATE entry)

**[ACTION]:** Refresh or navigate back to the audit log for the same record.

**[SAY]:**
> "The UPDATE entry has appeared immediately. Old value shows IN_PROGRESS,
> new value shows NON_COMPLIANT. This gives us a complete compliance change history —
> exactly what PCI-DSS auditors require."

**[EXPECTED]:** Audit log shows both `CREATE` and `UPDATE` entries in chronological order.

---

## BEAT 4 — Search and Filter

**[ACTION]:** Navigate to the records list. Type `firewall` in the search bar. Then select `NON_COMPLIANT` from the status dropdown.

**[SAY]:**
> "Search is debounced and paginated. Results update as you type.
> You can filter by status and sort by any column."

**[EXPECTED]:** Filtered results appear correctly with matching records only.

---

## BEAT 5 — CSV Export

**[ACTION]:** As ADMIN user, click the `Export CSV` button.

**[SAY]:**
> "ADMIN and MANAGER roles can export all active records to CSV.
> VIEWER role is blocked. The filename includes today's date automatically."

**[EXPECTED]:** Browser downloads `compliance_records_2026-05-09.csv`.

---

## BEAT 6 — Swagger UI

**[ACTION]:** Switch to Tab 2 — `http://localhost:8080/swagger-ui/index.html`

**[SAY]:**
> "All 14 endpoints are documented in Swagger with request and response examples.
> This is auto-generated from our OpenAPI annotations — no manual documentation needed."

**[EXPECTED]:** Swagger UI loaded, showing all controllers — Auth, Compliance Records, Audit Log, Export.

---

## [TRANSITION OUT]

**[SAY]:**
> "I will hand over to [Java Developer 3] to show the dashboard and analytics."

---

## If Something Goes Wrong

| Issue | Recovery |
|---|---|
| App not responding | Say "Let me show you this from our backup" — open backup_javadev2_demo_section.mp4 |
| Audit log empty | Check backend logs: `docker logs backend` — show the terminal log output instead |
| CSV not downloading | Show the file already saved to desktop from the morning test |
| Swagger not loading | Navigate to `http://localhost:8080/v3/api-docs` and explain the JSON spec |
