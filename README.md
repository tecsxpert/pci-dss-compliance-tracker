# PCI-DSS Compliance Tracker

## Overview

The PCI-DSS Compliance Tracker is a full-stack web application built for security and compliance teams responsible for maintaining Payment Card Industry Data Security Standard (PCI-DSS) controls. It provides a centralised platform for creating, assigning, tracking, and auditing compliance records across all 12 PCI-DSS requirement domains. The tool solves the problem of scattered spreadsheet-based tracking by replacing it with a secure, role-based system that enforces data integrity, automatically logs every change, sends deadline alert emails, and generates exportable compliance reports — making audit preparation faster and more reliable.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT BROWSER                        │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP :80
┌──────────────────────────────▼──────────────────────────────┐
│                    FRONTEND (React + Vite)                    │
│                         Nginx :80                            │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP :8080
┌──────────────────────────────▼──────────────────────────────┐
│              BACKEND (Spring Boot 3 / Java 17)               │
│  REST API │ JWT Auth │ Redis Cache │ Scheduler │ Email        │
└────────┬─────────────────────┬────────────────┬─────────────┘
         │                     │                │
┌────────▼───────┐   ┌─────────▼──────┐  ┌─────▼──────────────┐
│  PostgreSQL 15 │   │    Redis 7      │  │  AI Service (Flask) │
│  Primary DB    │   │  Cache Layer    │  │  Groq + ChromaDB   │
└────────────────┘   └────────────────┘  └────────────────────┘
```

---

## Prerequisites

- **Docker Desktop** (version 24+) — with WSL2 Linux engine enabled on Windows
- **Docker Compose** (version 2.x — bundled with Docker Desktop)
- **Git** (version 2.x)
- **A Groq API key** — free at [console.groq.com](https://console.groq.com) — no credit card required
- **A Gmail account or SMTP server** — for email notifications

---

## Environment Setup

```bash
cp .env.example .env
# Open .env and fill in all values before running docker-compose
```

| Variable Name | Description | Example Value |
|---|---|---|
| `POSTGRES_DB` | Name of the PostgreSQL database | `pci_dss_db` |
| `POSTGRES_USER` | PostgreSQL superuser username | `pci_user` |
| `POSTGRES_PASSWORD` | PostgreSQL superuser password | `change_me_db_password` |
| `REDIS_PASSWORD` | Redis requirepass value | `change_me_redis_password` |
| `JWT_SECRET` | HMAC-SHA256 signing secret (≥32 chars) | `change_me_super_long_jwt_secret` |
| `JWT_EXPIRY_MS` | Access token TTL in milliseconds | `3600000` (1 hour) |
| `REFRESH_TOKEN_EXPIRY_MS` | Refresh token TTL in milliseconds | `604800000` (7 days) |
| `MAIL_HOST` | SMTP server hostname | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | SMTP login username | `your-email@gmail.com` |
| `MAIL_PASSWORD` | SMTP app password | `your_app_password` |
| `MAIL_FROM` | From address in outgoing emails | `noreply@example.com` |
| `GROQ_API_KEY` | Groq cloud LLM API key | `sk-or-v1-...` |
| `AI_SERVICE_URL` | AI service URL (inside Docker network) | `http://ai-service:5000` |
| `BACKEND_PORT` | Host port mapped to backend :8080 | `8080` |
| `AI_PORT` | Host port mapped to ai-service :5000 | `5000` |
| `SCHEDULER_ADMIN_EMAIL` | Receives daily compliance alert emails | `admin@example.com` |
| `DB_POOL_SIZE` | HikariCP maximum connection pool size | `20` |
| `DB_POOL_MIN_IDLE` | HikariCP minimum idle connections | `5` |
| `SPRING_PROFILE` | Active Spring profile (`prod` or `dev`) | `prod` |

---

## Running the Application

```bash
git clone <repository-url>
cd pci-dss-compliance-tracker
cp .env.example .env
# Edit .env with your actual values
docker-compose up --build
```

**Expected healthy startup output:**

- PostgreSQL: `database system is ready to accept connections`
- Redis: `Ready to accept connections`
- Backend: `Started PciDssComplianceTrackerApplication in X seconds`
- AI Service: `Running on http://0.0.0.0:5000`
- Frontend: `nginx: worker process started`

Full stack typically healthy in under 3 minutes.

---

## Verifying the Stack

| Service | URL |
|---|---|
| Backend health | http://localhost:8080/actuator/health |
| API documentation | http://localhost:8080/swagger-ui/index.html |
| AI service health | http://localhost:5000/health |
| Frontend | http://localhost |

---

## Default Demo Credentials

| Role | Username | Password |
|---|---|---|
| ADMIN | admin | (see .env) |
| MANAGER | manager | (see .env) |
| VIEWER | viewer | (see .env) |

> Passwords are set via the `.env` file and seeded on first application startup via Flyway.

---

## Running Tests

```bash
# Run all unit tests and generate JaCoCo coverage report
mvn clean verify jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

Coverage threshold: **80% line coverage** — build fails if below threshold.

---

## Resetting Demo Data

```bash
docker-compose down -v
docker-compose up --build
# Wipes all data and re-seeds 30 fresh demo compliance records
```

---

## Known Issues

| Bug ID | Severity | Description | Workaround |
|---|---|---|---|
| P3-001 | P3 | Swagger UI shows generic 500 for some validation errors instead of 400 | Use request body schema hints in Swagger to avoid triggering |
| P3-002 | P3 | Email not sent in dev profile when MAIL_PASSWORD is blank | Set a valid SMTP credential or use Mailhog |

---

## Team

| Role | Member |
|---|---|
| Project Lead | TBC |
| Java Developer 1 | TBC |
| Java Developer 2 | TBC |
| Java Developer 3 | TBC |
| Frontend Developer | TBC |
| AI/ML Engineer | TBC |
| DevOps Engineer | TBC |