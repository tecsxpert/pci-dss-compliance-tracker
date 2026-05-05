# Q&A Answer Cards — Java Developer 2
## PCI-DSS Compliance Tracker — Demo Day 9 May 2026

Memorise all answers below. Do not read from this card during Q&A.

---

## Q1: What is the audit log and why does it matter for PCI-DSS compliance?

**A:**
> "PCI-DSS requires a complete trail of who accessed or changed cardholder data controls.
> Our audit log captures every create, update, and delete automatically using Spring AOP —
> no developer has to remember to log anything. It records the user, timestamp,
> and full before/after JSON snapshots. This is non-intrusive and resilient —
> audit failures are logged and swallowed so they never break the main operation."

---

## Q2: Why does the CSV export check the user role?

**A:**
> "Exporting compliance data in bulk is a sensitive operation. VIEWER role users
> can read individual records but cannot bulk-export the entire dataset.
> This limits data exfiltration risk — a key PCI-DSS requirement.
> The check is enforced at the controller layer using Spring Security's @PreAuthorize annotation."

---

## Q3: What happens to deleted records?

**A:**
> "We use soft delete — records are never physically removed from the database.
> The is_deleted flag is set to true, they disappear from all queries and exports,
> but the data and audit trail are preserved.
> This is required for compliance audit history — PCI-DSS mandates that historical records
> are retained for at least one year."

---

## Q4: How does the scheduler work?

**A:**
> "Spring's @Scheduled annotation runs three cron jobs:
> a daily overdue reminder at 8 AM,
> a 7-day advance deadline alert at 9 AM,
> and a weekly summary every Monday at 7 AM.
> All cron expressions are in application.yml — not hardcoded —
> so they can be changed without redeployment."

---

## Q5: What does the PCI-DSS Compliance Tracker do? (one sentence)

**A:**
> "It is a secure, role-based web application that lets compliance teams create,
> assign, track, and audit PCI-DSS control records — with automatic email alerts,
> AI-powered recommendations, and a full change history for every record."

---

## Q6: What AI model does the AI service use and why?

**A:**
> "It uses Llama 3 8B served through the Groq cloud API.
> We chose Groq because it offers extremely fast inference speeds —
> under 500ms for most responses — and the API is free with no credit card required,
> which is ideal for a capstone demo environment."

---

## Q7: What is RAG and how is it used in this tool?

**A:**
> "RAG stands for Retrieval-Augmented Generation.
> Instead of relying only on the LLM's training data,
> we first retrieve relevant PCI-DSS documentation from ChromaDB — a vector store —
> and inject it into the prompt context.
> This means the AI's recommendations are grounded in actual PCI-DSS requirement text,
> not just general knowledge."

---

## Q8: What security measures protect the API?

**A:**
> "We use JWT-based authentication with role-based access control on every endpoint.
> Tokens are blacklisted in Redis on logout — so stolen tokens are immediately invalidated.
> All sensitive config values are in environment variables, never hardcoded.
> Spring Security enforces @PreAuthorize annotations at the method level for ADMIN, MANAGER, and VIEWER roles."

---

## Q9: What happens if the Groq API goes down during the demo?

**A:**
> "The AI service has a try/except handler that returns a graceful fallback response
> instead of crashing. The rest of the application — records, audit log, search, export —
> continues to work completely independently of the AI service.
> The services are decoupled by design."

---

## Q10: Why use Redis for the JWT blacklist instead of a database?

**A:**
> "Redis is an in-memory store with native TTL support.
> When a user logs out, we store the token in Redis with an expiry matching
> the token's remaining lifetime — it auto-deletes when it would have expired anyway.
> This is much faster than a database lookup on every authenticated request,
> and it avoids polluting the primary database with session data."
