# GitHub Submission Checklist — Day 18
## Date: 2026-05-07

Complete every item before the Day 18 submission commit.
Run each verification command and record the result.

---

## Repository Settings

- [ ] Repository is set to **Public** visibility on GitHub
- [ ] Branch `dhanush` is up to date with all committed work
- [ ] Tag `release/v1.0` exists — verify: `git tag -l`

---

## Security — No Secrets Committed

```bash
# Verify .env is NOT tracked by git
git ls-files | grep "^\.env$"
# Expected output: (empty — no output means .env is correctly ignored)

# Verify no API keys or secrets in staged files
git grep -r "sk-or-v1" -- "*.java" "*.yml" "*.properties" "*.env"
# Expected output: (empty)
```

- [ ] `.env` is NOT present in the repository
- [ ] `.env.example` IS present with all 20 variables listed
- [ ] No hardcoded secrets found in any source file

---

## .gitignore Completeness

Confirm `.gitignore` includes all of the following:

- [ ] `.env`
- [ ] `target/`
- [ ] `node_modules/`
- [ ] `__pycache__/`
- [ ] `chroma_data/`

---

## Commit History

```bash
# Verify daily commits exist
git log --oneline | head -20
```

- [ ] At least one commit exists for every working day (Day 1 through Day 18)
- [ ] No commit message contains "fix" without a description of what was fixed
- [ ] Commit messages follow format: `Day N — [description]`

---

## Required Files Present

```bash
# Verify all Flyway migrations present
ls backend/src/main/resources/db/migration/
# Expected: V1__init.sql  V2__audit.sql  V3__roles_seed.sql  V4__performance_indexes.sql
```

- [ ] `docker-compose.yml` is present at the project root
- [ ] `README.md` is complete and renders correctly on GitHub (check on github.com)
- [ ] `V1__init.sql`, `V2__audit.sql`, `V3__roles_seed.sql`, `V4__performance_indexes.sql` all present
- [ ] `backend/check_health.sh` present
- [ ] `.env.example` present with all variables

---

## No Compiled Artefacts Committed

```bash
# Check for accidentally committed .class or .jar files
git ls-files | grep -E "\.(class|jar|war)$"
# Expected: (empty)

# Check for node_modules
git ls-files | grep "node_modules"
# Expected: (empty)
```

- [ ] No `.class`, `.jar`, or `.war` files in git
- [ ] No `node_modules/` in git
- [ ] No `target/` directory in git

---

## Final Verification — Swagger UI

- [ ] Run `docker-compose up --build`
- [ ] Confirm Swagger UI accessible: `http://localhost:8080/swagger-ui/index.html`

---

## Day 18 Submission Commit Commands

```bash
git add .
git status        # Review carefully — confirm no secrets, no compiled files
git commit -m "Day 18 — GitHub submission: README complete, release/v1.0 tagged, all deliverables verified"
git push origin dhanush
```

---

## Fresh Machine Test Protocol

Test on a clean environment (or ask a teammate to clone and run):

```bash
git clone <repository-url>
cd pci-dss-compliance-tracker
cp .env.example .env
# Fill in .env values
docker-compose up --build
```

**Expected timeline:** Full stack healthy in under 3 minutes.

**Verification commands:**
```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

curl http://localhost:5000/health
# Expected: {"status":"ok"} or similar

curl -s -o /dev/null -w "%{http_code}" http://localhost
# Expected: 200
```

> If any step fails on a clean machine — it will fail on Demo Day. Fix it today.
