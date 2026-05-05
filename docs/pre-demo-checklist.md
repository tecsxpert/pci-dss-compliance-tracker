# Pre-Demo Morning Checklist — Java Developer 2
## Demo Day: Friday 9 May 2026

Complete every item **before entering the presentation room.**
Start this checklist at least 45 minutes before the presentation.

---

## System Setup

- [ ] **Laptop plugged in** — not running on battery
- [ ] Run clean stack startup:
  ```bash
  docker-compose down -v
  docker-compose up --build
  ```
- [ ] Wait for all 5 services to confirm healthy in logs:
  - [ ] PostgreSQL: `database system is ready to accept connections`
  - [ ] Redis: `Ready to accept connections`
  - [ ] Backend: `Started PciDssComplianceTrackerApplication`
  - [ ] AI Service: `Running on http://0.0.0.0:5000`
  - [ ] Frontend: `nginx: worker process started`
- [ ] Run health check script: `bash backend/check_health.sh`
  - [ ] Backend: **PASS**
  - [ ] AI Service: **PASS**
  - [ ] Frontend: **PASS**

---

## Data Verification

- [ ] **30 seeded records visible** on the frontend list page at `http://localhost`
- [ ] At least one record has status `IN_PROGRESS` (for the live status update demo)
- [ ] Audit log shows entries after a test update (do one test update, then revert or use a separate record)
- [ ] Email notification sends successfully — check mail logs:
  ```bash
  docker logs backend 2>&1 | grep -i "email\|mail\|sending"
  ```

---

## Feature Verification

- [ ] Search returns correct filtered results (test with keyword `firewall`)
- [ ] Status filter dropdown works (`NON_COMPLIANT` filter returns correct records)
- [ ] CSV export downloads with today's date in filename: `compliance_records_2026-05-09.csv`
- [ ] Status update reflects immediately with audit trail entry

---

## Browser Tabs — Open and Ready

Open these tabs in order before the presentation:

- [ ] **Tab 1:** `http://localhost` — Frontend list page (all 30 records visible)
- [ ] **Tab 2:** `http://localhost:8080/swagger-ui/index.html` — Swagger UI loaded
- [ ] **Tab 3:** Audit log view for a specific record (record created in morning test)
- [ ] **Tab 4:** Mailhog or backend logs terminal for email proof

---

## Terminal

- [ ] Terminal window open with:
  ```bash
  docker-compose logs -f backend
  ```
  Minimise but keep open — use if you need to show real-time logs.

---

## Backup Materials

- [ ] Backup screenshots folder on desktop — one click away
- [ ] Backup video `backup_javadev2_demo_section.mp4` on desktop
- [ ] Same backup stored on USB drive AND Google Drive

---

## Personal Readiness

- [ ] Demo script reviewed one final time (do NOT bring it on paper)
- [ ] Your section memorised — no reading during the demo
- [ ] Stopwatch tested — your section fits within 90 seconds
- [ ] Phone on **silent**
- [ ] Browser notifications **disabled**
- [ ] Screen **not set to sleep** (disable auto-sleep for the day)

---

## Sign-Off

| Check | Completed By | Time |
|---|---|---|
| All 5 services healthy | | |
| 30 records visible | | |
| Backup on USB | | |
| Script memorised | | |
| Ready to present | | |
