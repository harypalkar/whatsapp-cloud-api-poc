# MASTER-01 — Project Documentation

## 1. Product summary

**WhatsFlow** is an enterprise multi-tenant WhatsApp SaaS platform. Businesses self-onboard, connect their own Meta WhatsApp Business Accounts via Embedded Signup, import customers, run marketing campaigns, operate a shared agent inbox, build forms and automations, and manage billing — without writing code.

Comparable products: Interakt, WATI, AiSensy, Gallabox, Twilio WhatsApp, Respond.io, Gupshup.

---

## 2. Stakeholders

| Role | Interest |
|---|---|
| Platform Owner | Revenue, abuse control, uptime |
| Company Admin | Onboarding, WhatsApp connect, campaigns, billing |
| Agent / Supervisor | Inbox productivity, SLA |
| End Customer | Timely, relevant WhatsApp communication |
| Engineering | Modular, testable, scalable codebase |
| Compliance | Audit, opt-in, data isolation |

---

## 3. Business capabilities checklist

- [ ] Register & verify email  
- [ ] Choose subscription  
- [ ] Connect Meta Business (Embedded Signup)  
- [ ] Connect WhatsApp Business number  
- [ ] Import customers  
- [ ] Send promotional campaigns  
- [ ] Receive customer replies  
- [ ] Assign chats to agents  
- [ ] Upload images / videos / PDFs  
- [ ] Create forms  
- [ ] Build automations  
- [ ] View analytics  
- [ ] Manage subscriptions  

---

## 4. Tech stack (locked)

| Layer | Choice |
|---|---|
| Language | Java 21 |
| Backend | Spring Boot 3.5.x, Security, JWT, Refresh, OAuth2, Data JPA, Hibernate, Validation, MapStruct, Lombok, springdoc |
| DB | PostgreSQL + Flyway |
| Cache / queue | Redis (Kafka-ready ports) |
| Realtime | WebSocket |
| Media | MinIO |
| Frontend | Angular 20, Angular Material, Bootstrap, Chart.js |
| Edge | Nginx |
| Containers | Docker |
| CI | GitHub Actions |
| Observability | Prometheus, Grafana, ELK |

---

## 5. Architecture principles

1. Clean Architecture + Hexagonal ports/adapters  
2. DDD module boundaries  
3. SOLID  
4. CQRS-ready command/query split  
5. Event-driven integration between modules  
6. Modular monolith now → microservices later  
7. Strict tenant isolation everywhere  

---

## 6. Document map

See [00-INDEX.md](00-INDEX.md) for the full MASTER-01 pack.

| Need | Go to |
|---|---|
| Big picture | 01 HLD |
| Use cases / ports | 02 LLD |
| C4 | 03 |
| ER / schema | 04, 05 |
| Repo layout | 06 |
| Engineering rules | 07 |
| Phased plan | 08 |
| Diagrams | 09 |

---

## 7. Validation reference (Altitude Labs)

Used to prove WhatsApp Cloud API behaviors during POC:

| Item | Value |
|---|---|
| Business | Altitude Labs |
| Number | +91 95126 18333 |
| Phone Number ID | 1226308087231072 |
| WABA | 1583394760167591 |
| Marketing template | `altitude_welcome_promo` (APPROVED) |
| Known Meta filters | `#131047` (outside 24h), `#131049` (ecosystem engagement) |

POC learnings feed WhatsFlow product rules (template-first cold outreach, webhook error surfacing, quality pacing).

---

## 8. Branching

| Branch | Use |
|---|---|
| `main` | Stable POC / release line |
| `develop` | Integration |
| `feature/business_model_whatsapp` | WhatsFlow SaaS architecture & upcoming implementation (**active**) |

---

## 9. Out of scope for MASTER-01

- Application code generation  
- Flyway SQL files  
- Angular project generation  
- CI pipeline implementation  
- Live Embedded Signup coding  

---

## 10. Approval gate

**MASTER-01 complete — awaiting MASTER-02.**

No implementation work proceeds until MASTER-02 is issued.
