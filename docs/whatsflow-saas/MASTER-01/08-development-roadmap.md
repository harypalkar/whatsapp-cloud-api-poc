# MASTER-01 — Development Roadmap

## Gate policy

| Gate | Output | Implementation? |
|---|---|---|
| **MASTER-01** | Architecture pack (this folder) | **No** |
| **MASTER-02** | TBD (await instruction) | Only after issued |
| Later MASTERs | Scaffold → DB → API → Web → Meta → Campaigns → Inbox → Ops | Yes |

---

## Delivery waves (post MASTER-01)

### Wave A — Foundation

1. Monorepo scaffold (`apps/api`, `apps/worker`, `apps/web`, `deploy`)  
2. Shared kernel: tenant context, security skeleton, error model  
3. Flyway V1–V3: companies, users, RBAC, auth tokens  
4. Register / verify email / login / refresh  
5. Docker Compose: PG, Redis, MinIO, Mailhog  

**Exit:** Company can register and obtain JWT.

### Wave B — Tenant + Billing + Meta connect

1. Company settings  
2. Plans + subscription entitlements (manual/Stripe later)  
3. Meta Embedded Signup start/complete  
4. Encrypted `whatsapp_accounts` storage  
5. Template sync from Graph  

**Exit:** Tenant connects own WABA/phone without engineering help.

### Wave C — CRM + Campaigns

1. Customer CRUD + CSV import + opt-in  
2. Media upload (MinIO)  
3. Campaign create/schedule  
4. Worker blast with throttle + recipient state  
5. Webhook status → delivery analytics (`131047` / `131049` mapped)  

**Exit:** Altitude-class promo blast works for approved marketing templates.

### Wave D — Inbox + Agents

1. Inbound message → conversation  
2. Shared inbox UI + WebSocket  
3. Assignment / transfer / close  
4. Session replies inside 24h window  
5. Agent presence  

**Exit:** Human agents handle replies at scale.

### Wave E — Automation + Forms

1. Forms builder + public submit  
2. Workflow engine MVP (trigger → actions)  
3. Notifications  
4. Audit completeness  

**Exit:** No-code automation for common journeys.

### Wave F — Analytics + Platform Admin + Hardening

1. Chart.js dashboards (delivery, campaigns, SLA)  
2. Platform admin portal  
3. Rate limits, WAF rules, secret rotation runbooks  
4. Prometheus / Grafana / ELK wiring  
5. Load test plan (10k tenants synthetic, campaign soak)  
6. Kafka adapter behind existing ports (optional)  

**Exit:** Production readiness review.

---

## Milestone map vs modules

| Milestone | Modules unlocked |
|---|---|
| M1 Auth | Authentication, Authorization, Company |
| M2 Connect | Meta Embedded Signup, WhatsApp Cloud API (admin) |
| M3 Engage | CRM, Campaign, Media |
| M4 Converse | Conversation, Shared Inbox, Agent |
| M5 Automate | Workflow, Forms, Notification |
| M6 Operate | Reports, Analytics, Audit, Settings, Admin, Billing polish |

---

## Parallelization

| Stream | Owners |
|---|---|
| Backend modules | Java team |
| Angular features | Web team |
| Meta integration | Integration specialist |
| DevOps | CI, Compose, observability |
| QA | Tenant isolation + Meta sandbox E2E |

---

## Risks & mitigations

| Risk | Mitigation |
|---|---|
| Meta template / `#131049` filters | Product messaging + pacing + quality monitoring |
| Token leakage | Encrypted storage, never log, rotate |
| Tenant leakage | Mandatory tests + review checklist |
| Campaign thundering herd | Worker concurrency caps + Redis backpressure |
| Monolith complexity | Strict module boundaries + ADRs |

---

## Immediate next step

**Stop after MASTER-01.**  
Await **MASTER-02** for the next authorized deliverable (expected: scaffold and/or database implementation plan).
