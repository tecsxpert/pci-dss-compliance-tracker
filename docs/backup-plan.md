# Backup Plan — Java Developer 2 Demo Section
## PCI-DSS Compliance Tracker — Demo Day 9 May 2026

This plan activates if the live system fails during your section of the demo.

---

## Day 19 — Create Your Backup Materials (Thursday 8 May)

### Screenshots to capture (1920×1080, PNG)

Save all in a folder named `backup_screenshots/` on the Desktop.

| Filename | What to capture |
|---|---|
| `tool17_audit_log_create_2026-05-08.png` | Audit log showing a CREATE entry with all fields |
| `tool17_audit_log_update_2026-05-08.png` | Audit log showing both CREATE and UPDATE entries |
| `tool17_email_log_2026-05-08.png` | Backend terminal showing email send confirmation |
| `tool17_search_filter_2026-05-08.png` | Records list filtered by keyword + NON_COMPLIANT status |
| `tool17_csv_download_2026-05-08.png` | Browser download bar showing compliance_records_2026-05-08.csv |
| `tool17_swagger_ui_2026-05-08.png` | Swagger UI showing all endpoint groups |
| `tool17_dashboard_2026-05-08.png` | Dashboard with 4 KPI cards and real seeded data |

---

### Screen Recording

Record a continuous 60–90 second screen recording of your full demo section working correctly.

**Steps to record (Windows):**
```
Win + G → Capture → Start Recording
```
or use OBS Studio.

**Save as:** `backup_javadev2_demo_section.mp4`

**Storage locations (store in ALL THREE):**
- [ ] Desktop of the demo laptop
- [ ] USB drive (label: DEMO DAY BACKUP)
- [ ] Google Drive — shared folder with team

---

## Demo Day — Failure Recovery Protocol

### If the entire application is down (docker-compose failure):

1. Say calmly: *"Let me switch to our backup while we investigate the issue."*
2. Open `backup_javadev2_demo_section.mp4` on Desktop
3. Play the video and narrate over it exactly as you would in the live demo
4. Use your demo script (this document) for the narration — same words, same beats

### If a specific feature fails (partial failure):

| What Fails | Recovery Action |
|---|---|
| Audit log not loading | Open screenshot `tool17_audit_log_create_2026-05-08.png` and narrate |
| Email not sending | Show `tool17_email_log_2026-05-08.png` — "here is the log from our test run" |
| Search not filtering | Refresh page — if still broken, use screenshot |
| CSV not downloading | Show pre-downloaded CSV file from Desktop |
| Swagger UI not loading | Navigate to `http://localhost:8080/v3/api-docs` — show raw JSON and explain |

### Key phrases if things go wrong:

- *"Let me show you this from our test run earlier this morning."*
- *"The feature works as expected — let me walk you through what you would normally see."*
- *"This is a known network issue with the demo environment — the code is correct."*

---

## Screenshot Naming Convention

All portfolio screenshots follow this format:
```
tool17_{description}_{date}.png
```

Examples:
- `tool17_dashboard_2026-05-09.png`
- `tool17_audit_log_2026-05-09.png`
- `tool17_swagger_ui_2026-05-09.png`
- `tool17_csv_export_2026-05-09.png`
- `tool17_search_filter_2026-05-09.png`

---

## Portfolio Screenshot Instructions

Capture at **1920×1080 resolution** with browser at 100% zoom.

| # | Screenshot | What Must Be Visible |
|---|---|---|
| 1 | Dashboard | 4 KPI cards with real numeric values from seeded data |
| 2 | Record Detail | Colour-coded status badge, all fields populated, no empty cells |
| 3 | AI Analysis Panel | AI recommendations text loaded for a real record |
| 4 | Analytics Page | Bar chart, line chart, or pie chart with real data labels |
| 5 | Swagger UI | Full list of endpoints at `/swagger-ui/index.html`, all controllers visible |
