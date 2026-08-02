# MASTER-01 — High Level Design (HLD)

## 1. Vision

WhatsFlow is a multi-tenant SaaS platform that lets **unlimited businesses** self-serve:

Register → verify email → subscribe → connect Meta / WhatsApp via **Embedded Signup** → import customers → run campaigns → receive replies → assign agents → automate → analyze → bill.

Business users require **zero coding**. Platform owner operates the control plane for all tenants.

---

## 2. Scale targets

| Dimension | Target |
|---|---|
| Tenants (companies) | 100,000+ |
| Concurrent conversations | Millions |
| Campaign throughput | Horizontal workers; Redis/Kafka fan-out |
| Availability | API 99.9% (design goal) |
| Isolation | Zero cross-tenant data leakage |

---

## 3. Actors

| Actor | Responsibility |
|---|---|
| Platform Owner / Super Admin | Plans, global config, abuse, support tools |
| Company Admin | Users, billing, WhatsApp connect, settings |
| Agent / Supervisor | Shared inbox, assignments, replies |
| Automation Engine | Workflows, triggers, bot replies |
| End Customer | WhatsApp user chatting with a company |
| Meta | Cloud API, webhooks, Embedded Signup, templates |

---

## 4. Logical architecture

```
┌─────────────────────────────────────────────────────────────┐
│ Clients: Angular (Agent / Admin) · Public Forms · Meta WA   │
└────────────────────────────┬────────────────────────────────┘
                             │ HTTPS / WSS
┌────────────────────────────▼────────────────────────────────┐
│ Edge: Nginx (TLS, static, rate limit, webhook path)         │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│ WhatsFlow Modular Monolith (Spring Boot 3.5 / Java 21)      │
│  Identity · Tenant · Billing · Meta · WhatsApp · CRM        │
│  Campaign · Conversation · Inbox · Agents · Workflow        │
│  Forms · Notifications · Reports · Analytics · Audit        │
│  Settings · Admin Portal APIs                               │
└───┬──────────────┬──────────────┬──────────────┬────────────┘
    │              │              │              │
 PostgreSQL      Redis         MinIO      Meta Cloud API
 (tenants)    (cache/queue)   (media)   (WA + Embedded Signup)
    │
 Worker process(es) — campaigns, webhooks, automation
 Kafka-ready publisher/consumer ports (adapter later)
```

---

## 5. Module catalog (business capabilities)

| Module | Capability |
|---|---|
| Authentication | Register, login, email verify, password reset, JWT + refresh |
| Authorization | RBAC roles/permissions per company + platform roles |
| Company / Tenant | Tenant lifecycle, branding, settings, isolation context |
| Billing / Subscription | Plans, invoices, payment hooks, feature entitlements |
| Meta Embedded Signup | OAuth-style Meta connect, WABA + phone onboarding |
| WhatsApp Cloud API | Send/receive text, template, media, location, buttons |
| Customer CRM | Contacts, tags, segments, import/export, opt-in/out |
| Campaign Management | Audience, schedule, throttle, template vars, reports |
| Conversation | Thread model, 24h window awareness, status timeline |
| Shared Inbox | Multi-agent queue, assignment, presence |
| Agent Management | Seats, skills, shifts, supervisor controls |
| Workflow Automation | Trigger → condition → action graphs |
| Forms Builder | Drag-drop forms, public links, WhatsApp deep links |
| Notification | In-app, email (later), webhook callbacks to tenants |
| Reports / Analytics | Delivery, read, campaign ROI, agent SLA |
| Audit | Immutable security/compliance trail |
| Settings | Company prefs, API keys, webhooks outbound |
| Admin Portal | Platform-owner console |

---

## 6. Multi-tenancy model

```
Platform Owner
   └── Company A (tenant)
   └── Company B (tenant)
   └── Company C (tenant)
   └── … unlimited
```

Each company owns: users, roles, WhatsApp numbers, customers, campaigns, forms, templates, media, reports, billing.

**Isolation rules (non-negotiable):**

1. Every business row carries `company_id` (UUID FK → `companies`).
2. `TenantContext` is resolved from JWT (or API key) at the edge of the application layer.
3. Repositories **must** filter by `company_id`; platform admin paths are explicit and audited.
4. Meta webhooks resolve tenant via `phone_number_id` → `whatsapp_accounts.company_id`.
5. Object storage keys are prefixed `/{companyId}/…`.
6. Cache keys are prefixed `tenant:{companyId}:…`.
7. No shared mutable global state between tenants.

---

## 7. Cross-cutting concerns

| Concern | Approach |
|---|---|
| Security | JWT, refresh rotation, BCrypt/Argon2, AES-GCM secrets, CSRF for cookie paths, XSS-safe UI, rate limits |
| Observability | Micrometer → Prometheus → Grafana; structured logs → ELK; audit table |
| Resilience | Retries with jitter for Meta; idempotent webhooks; DLQ for failed jobs |
| Consistency | DB transactions in application services; outbox for domain events (CQRS/event ready) |
| Media | Validate MIME/size; virus scan hook; store in MinIO; Meta media upload via provider port |
| Meta constraints | Marketing templates for cold send; free-form only inside 24h customer-care window; honor `#131047` / `#131049` |

---

## 8. Deployment topology (target)

| Tier | Components |
|---|---|
| Edge | Nginx (TLS termination, static Angular, API reverse proxy) |
| App | `whatsflow-api` (N replicas), `whatsflow-worker` (N replicas) |
| Data | PostgreSQL primary + replicas (later), Redis cluster, MinIO |
| Async (phase) | Kafka (optional adapter behind same ports) |
| Obs | Prometheus, Grafana, ELK |
| CI/CD | GitHub Actions → build, test, image push, deploy |

---

## 9. Non-goals (MASTER-01)

- No application source generation
- No Flyway SQL files yet
- No Angular scaffolding yet
- No live Meta Embedded Signup integration code yet

---

## 10. Success criteria for HLD

- Self-serve business onboarding path is complete end-to-end conceptually  
- Tenant isolation is explicit and enforceable  
- Modules are independently extractable  
- Stack matches enterprise brief (Java 21 / Spring Boot 3.5 / Angular 20 / PG / Redis / MinIO / Docker)  
