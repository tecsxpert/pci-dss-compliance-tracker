# Bug Triage Template — PCI-DSS Compliance Tracker

Use this template for every P1/P2 bug found from Day 16 onwards.
P3 bugs are documented in README.md under "Known Issues" only.

---

## Bug Triage Entry

| Field | Value |
|---|---|
| **Bug ID** | BUG-XXX |
| **Date Found** | YYYY-MM-DD |
| **Found By** | [Team member name/role] |
| **Severity** | P1 / P2 / P3 |
| **Component** | Backend / Frontend / AI Service / Docker / Database |
| **Description** | One sentence describing what is broken |
| **Steps to Reproduce** | 1. ... 2. ... 3. ... |
| **Expected Behaviour** | What should happen |
| **Actual Behaviour** | What actually happens |
| **Root Cause** | Technical explanation of why this is broken |
| **Fix Applied** | Description of the code/config change made |
| **Files Changed** | List of files modified |
| **Fix Committed** | Commit hash or N/A |
| **Tests Updated** | Yes / No — list affected test classes |
| **Verified By** | [Team member who confirmed fix works] |
| **Verified In** | Unit test / docker-compose / both |
| **Status** | OPEN / IN PROGRESS / FIXED / WONT FIX |

---

## Severity Definitions

| Severity | Definition | SLA |
|---|---|---|
| **P1** | App crashes, data loss, authentication bypass, docker-compose fails to start | Fix immediately — same hour |
| **P2** | Wrong data returned, missing audit log, email not sending, CSV export broken | Fix same day before EOD |
| **P3** | Minor UI cosmetic issues, label mismatches, non-breaking visual bugs | Document in README Known Issues; do not fix this week |

---

## Bug Log (all open and resolved)

| Bug ID | Severity | Component | Description | Status | Fixed By |
|---|---|---|---|---|---|
| | | | | | |
