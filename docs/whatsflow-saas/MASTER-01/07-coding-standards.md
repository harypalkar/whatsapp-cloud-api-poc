# MASTER-01 — Coding Standards

## 1. General

- Language of code, comments, commits, PRs: **English**
- Prefer clarity over cleverness
- No secrets in git (`.env`, tokens, private keys)
- Every PR maps to a module + acceptance criteria

---

## 2. Java / Spring Boot

| Rule | Standard |
|---|---|
| Version | Java 21, Spring Boot 3.5.x |
| Style | Google-like / project Checkstyle; Spotless optional |
| Nullability | Prefer `Optional` at boundaries; validate DTOs with Jakarta Validation |
| Transactions | `@Transactional` on application services only |
| Controllers | Thin — map DTO ↔ command/query; no business rules |
| Mapping | MapStruct for DTO ↔ command/view |
| Boilerplate | Lombok allowed (`@Getter`, `@Builder`); avoid `@Data` on entities |
| Logging | SLF4J; no PII / tokens; include `traceId`, `companyId` |
| Exceptions | Domain exceptions → global handler → stable error codes |
| Tests | Unit (domain/app) + slice + integration (Testcontainers PG/Redis) |
| Naming | `*UseCase` / `*CommandHandler`, `*QueryHandler`, `*Port`, `*Adapter` |

**Forbidden in domain:** Spring Web, JPA annotations (prefer infrastructure persistence models), Meta SDK types.

---

## 3. Multi-tenancy rules (hard)

1. Never accept `companyId` from client body for authorization — take from `TenantContext`.
2. Every repository query for tenant data includes `company_id`.
3. Platform admin endpoints are separate package + role + audit.
4. Integration tests must prove cross-tenant read returns 404/empty.

---

## 4. API standards

- Base path `/api/v1`
- JSON only; ISO-8601 timestamps
- Pagination: `page`, `size`, `sort` → envelope `{ items, page, size, total }`
- Idempotency-Key header for campaign schedule / payment confirm
- OpenAPI annotations mandatory on public controllers
- Breaking changes require version bump or compatibility window

---

## 5. Database standards

- Flyway forward-only
- UUID PKs; no sequential public IDs for tenants
- Soft delete default
- Indexes reviewed for every new query path
- No unbounded `SELECT *` list endpoints

---

## 6. Angular standards

| Rule | Standard |
|---|---|
| Version | Angular 20 |
| UI | Angular Material + Bootstrap utilities; Chart.js for analytics |
| Structure | Feature modules/folders; lazy routes |
| State | Signals / signal stores preferred; NgRx only if complexity demands |
| HTTP | Interceptors for JWT refresh + tenant headers |
| Forms | Reactive forms + typed models |
| A11y | Material defaults; contrast AA |
| Tests | Component + service unit tests for critical flows |

---

## 7. Security standards

- Password hashing Argon2id or BCrypt
- Refresh token rotation + reuse detection
- Rate limit auth and send endpoints
- Encrypt Meta tokens at rest (AES-GCM)
- Sanitize HTML in forms/inbox previews
- CSRF strategy documented for cookie-based flows
- Dependency scanning in CI

---

## 8. Git / CI

| Branch | Purpose |
|---|---|
| `main` | Production-ready |
| `develop` | Integration |
| `feature/*` | Features (current: `feature/business_model_whatsapp`) |

- Conventional commits preferred: `feat(campaign): …`, `fix(inbox): …`, `docs(arch): …`
- CI: compile, unit tests, lint, Docker build
- No `--no-verify` unless explicitly approved

---

## 9. Observability

- Metrics: Micrometer timers for Meta calls, campaign lag, webhook age
- Logs: JSON to ELK
- Traces: OpenTelemetry-ready (`traceId` propagation)
- Dashboards: Grafana for API latency, error rate, Meta code histogram

---

## 10. Definition of Done (feature)

- [ ] Tenant isolation covered by test  
- [ ] OpenAPI updated  
- [ ] Flyway migration (if schema) reviewed  
- [ ] Metrics/logs for failure paths  
- [ ] Docs / ADR if architectural  
- [ ] No secrets committed  
