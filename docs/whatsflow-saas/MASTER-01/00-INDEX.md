# WhatsFlow SaaS — MASTER-01 Architecture Pack

**Status:** Documentation only — **no implementation in MASTER-01**  
**Branch:** `feature/business_model_whatsapp`  
**Next gate:** Await **MASTER-02** before any code generation  

**Product class:** Interakt · WATI · AiSensy · Gallabox · Twilio WhatsApp · Respond.io · Gupshup  

---

## Deliverables

| # | Document | File |
|---|---|---|
| 1 | High Level Design | [01-high-level-design.md](01-high-level-design.md) |
| 2 | Low Level Design | [02-low-level-design.md](02-low-level-design.md) |
| 3 | C4 Architecture | [03-c4-architecture.md](03-c4-architecture.md) |
| 4 | ER Diagram | [04-er-diagram.md](04-er-diagram.md) |
| 5 | Database Design | [05-database-design.md](05-database-design.md) |
| 6 | Folder Structure | [06-folder-structure.md](06-folder-structure.md) |
| 7 | Coding Standards | [07-coding-standards.md](07-coding-standards.md) |
| 8 | Development Roadmap | [08-development-roadmap.md](08-development-roadmap.md) |
| 9 | Mermaid Diagrams | [09-mermaid-diagrams.md](09-mermaid-diagrams.md) |
| 10 | Project Documentation | [10-project-documentation.md](10-project-documentation.md) |

---

## Locked decisions (MASTER-01)

| Area | Decision |
|---|---|
| Topology | Modular monolith (microservice-ready modules) |
| Style | Clean Architecture + DDD + Hexagonal + SOLID + CQRS-ready + Event-driven |
| Tenancy | Shared PostgreSQL schema, mandatory `company_id`, row-level isolation in every query |
| Auth | JWT access + refresh, OAuth2 (Meta Embedded Signup), RBAC |
| Data | PostgreSQL UUID PKs, soft delete, audit columns, optimistic locking, Flyway |
| Async | Redis (MVP) → Kafka-ready abstraction |
| Realtime | WebSocket (shared inbox) |
| Media | MinIO |
| Frontend | Angular 20 + Angular Material + Bootstrap + Chart.js |
| Edge / Ops | Nginx, Docker, GitHub Actions, Prometheus, Grafana, ELK |
| WhatsApp | Meta Cloud API + Embedded Signup; campaign cold outreach via approved marketing templates |

---

## Reference tenant (Altitude Labs — validation seed)

| Field | Value |
|---|---|
| Display name | Altitude Labs |
| WhatsApp | +91 95126 18333 |
| Phone Number ID | `1226308087231072` |
| WABA | `1583394760167591` |
| Approved marketing template | `altitude_welcome_promo` |
| Promo | `WELCOME100` |

---

## Gate

> **Do not generate application code, Flyway SQL, Angular apps, or Docker images until MASTER-02 is issued.**
