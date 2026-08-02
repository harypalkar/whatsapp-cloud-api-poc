# MASTER-01 — Low Level Design (LLD)

## 1. Hexagonal / Clean layering (per module)

```
com.whatsflow.<module>
  ├── api                 # Controllers, DTOs, OpenAPI, WebSocket endpoints
  ├── application         # Use cases, command/query handlers (CQRS-ready)
  │     ├── port.in       # Driving ports (use case interfaces)
  │     └── port.out      # Driven ports (repos, Meta, MinIO, bus)
  ├── domain              # Aggregates, entities, value objects, domain events
  └── infrastructure      # JPA, Redis, Meta HTTP, MinIO, Kafka adapters
```

**Dependency rule:** `api` → `application` → `domain` ← `infrastructure`  
Infrastructure depends inward on ports; domain has **zero** Spring / JPA annotations when practical (or keep JPA in infrastructure mappers).

---

## 2. Shared kernel

| Component | Responsibility |
|---|---|
| `TenantContext` | Thread-bound / reactive-safe companyId + actorId |
| `TenantFilter` | Resolves JWT / API key → context |
| `SecurityConfig` | Resource server, CORS, CSRF policy, public routes |
| `DomainEvent` / `EventPublisher` | Outbox-friendly publish API |
| `PageRequest` / `ApiError` | Standard paging & error envelope |
| `SoftDeletable` / `Auditable` / `Versioned` | Base contracts |
| `SecretCrypto` | AES-GCM encrypt/decrypt for tokens |
| `Clock` / `IdGenerator` | Testable time & UUIDs |

---

## 3. Core use cases (selected)

### 3.1 Identity

| Use case | Input | Output | Notes |
|---|---|---|---|
| RegisterCompany | email, password, companyName | verification challenge | Creates company + owner user |
| VerifyEmail | token | session bootstrap | One-time token |
| Login | email, password | access + refresh | Rotate refresh |
| RefreshToken | refresh | new access (+ optional rotate) | Reuse detection |
| InviteUser | email, roleIds | invite link | Tenant-scoped |

### 3.2 Billing

| Use case | Notes |
|---|---|
| ChoosePlan | Attach subscription; gate features via entitlements |
| RecordPaymentWebhook | Idempotent payment provider events |
| EnforceEntitlement | Checked in application services before expensive ops |

### 3.3 Meta Embedded Signup

| Use case | Notes |
|---|---|
| StartEmbeddedSignup | Return Meta launch config / SDK session |
| CompleteEmbeddedSignup | Exchange code → store encrypted token, WABA, phone |
| SyncPhoneQuality | Periodic Graph pull (quality, limits) |

### 3.4 WhatsApp messaging

| Use case | Notes |
|---|---|
| SendTemplateMessage | Cold outreach; validate template status |
| SendSessionMessage | Requires open 24h window |
| SendMediaMessage | Upload to MinIO → Meta media → send |
| ProcessInboundWebhook | Idempotent; open/attach conversation; enqueue inbox event |
| ProcessStatusWebhook | Update delivery/read; surface Meta error codes |

### 3.5 Campaigns

| Use case | Notes |
|---|---|
| CreateCampaign | Draft + audience + template + media |
| ScheduleCampaign | Persist schedule; worker picks up |
| ExecuteCampaignBatch | Throttled fan-out; per-recipient state machine |
| Pause / Cancel | Cooperative cancel via Redis flag |

### 3.6 Inbox / Agents

| Use case | Notes |
|---|---|
| AssignConversation | Manual or round-robin / skill |
| AgentReply | Session message + WS broadcast |
| Transfer / Close | State transitions + audit |

### 3.7 Workflow / Forms

| Use case | Notes |
|---|---|
| SaveWorkflowDefinition | JSON graph validated |
| EvaluateTrigger | On inbound message / tag / form submit |
| PublishForm | Public tokenized URL |
| SubmitFormResponse | Create/update CRM + optional WA message |

---

## 4. Ports (driven)

| Port | Implementations |
|---|---|
| `WhatsAppProvider` | `MetaCloudApiWhatsAppProvider`, `MockWhatsAppProvider` |
| `MediaStorage` | `MinioMediaStorage` |
| `TokenEncryptor` | `AesGcmTokenEncryptor` |
| `JobQueue` | `RedisStreamJobQueue` → later `KafkaJobQueue` |
| `DomainEventBus` | `SpringApplicationEventBus` → later Kafka |
| `EmailSender` | SMTP / provider adapter |
| `PaymentGateway` | Stripe/Razorpay adapter (phase) |

---

## 5. CQRS readiness

| Side | Pattern |
|---|---|
| Commands | `*Command` + `*CommandHandler` mutate aggregates, emit events |
| Queries | `*Query` + `*QueryHandler` read models / projections |
| MVP | Same PostgreSQL for both; separate packages so read models can move |
| Projections (later) | Campaign analytics, inbox counters via event handlers |

---

## 6. Domain events (catalog sample)

| Event | When |
|---|---|
| `CompanyRegistered` | After company + owner created |
| `WhatsAppAccountConnected` | Embedded Signup complete |
| `CustomerImported` | Bulk import batch committed |
| `CampaignScheduled` | Campaign enters SCHEDULED |
| `CampaignRecipientAccepted` | Meta accepted wamid |
| `CampaignRecipientFailed` | Meta / filter failure (`131049`, etc.) |
| `MessageInboundReceived` | Customer message |
| `ConversationAssigned` | Agent assignment |
| `FormSubmitted` | Public form post |
| `SubscriptionChanged` | Plan change |

Events are published **after** successful transaction commit (transactional outbox pattern recommended).

---

## 7. API surface (sketch)

Base: `/api/v1`

| Area | Examples |
|---|---|
| Auth | `POST /auth/register`, `/login`, `/refresh`, `/verify-email` |
| Company | `GET/PATCH /company` |
| Billing | `GET /plans`, `POST /subscriptions` |
| Meta | `POST /meta/embedded-signup/start`, `/complete` |
| CRM | `GET/POST /customers`, `POST /customers/import` |
| Campaigns | `POST /campaigns`, `POST /campaigns/{id}/schedule` |
| Inbox | `GET /conversations`, `POST /conversations/{id}/messages` |
| Media | `POST /media` |
| Forms | `CRUD /forms`, public `POST /public/forms/{token}/submit` |
| Admin | `/platform/**` (super-admin only) |
| Webhooks | `GET/POST /webhooks/meta/whatsapp` (public + verify) |
| WS | `/ws/inbox` |

All authenticated REST calls carry tenant from JWT. Platform routes require `ROLE_PLATFORM_ADMIN`.

---

## 8. Security LLD

| Control | Design |
|---|---|
| Access token | Short TTL JWT (e.g. 15m): `sub`, `companyId`, `roles`, `jti` |
| Refresh token | Opaque, hashed at rest, rotate, family revoke on reuse |
| Password | Argon2id or BCrypt strength ≥ 12 |
| API keys | Prefixed keys, hashed storage, scoped permissions |
| Secrets | Meta tokens AES-GCM; DEK via env/KMS |
| Rate limit | Per IP + per company + per endpoint class |
| Webhook | Meta verify token + optional app secret HMAC |
| Tenant guard | AOP / repository aspect rejecting missing company filter |

---

## 9. Worker design

Separate Spring Boot process (same codebase, different main):

- Consumes Redis streams / Kafka topics: `campaign.send`, `webhook.process`, `automation.run`
- Idempotency keys on job id
- Backpressure via consumer groups
- Metrics: lag, success, Meta error code histogram

---

## 10. Frontend LLD (Angular 20)

| App area | Features |
|---|---|
| Auth | Register, verify, login, forgot password |
| Onboarding wizard | Plan → Embedded Signup → import sample customers |
| CRM | Contacts, segments, import CSV |
| Campaigns | Builder, audience, schedule, live stats |
| Inbox | Conversation list, thread, assign, media |
| Forms | Visual builder |
| Automations | Workflow canvas |
| Analytics | Chart.js dashboards |
| Billing | Plan, invoices, usage |
| Platform Admin | Tenants, abuse, global templates |

State: feature stores (NgRx or signal stores — decide in MASTER-02).  
Realtime: WebSocket service with reconnect + tenant-scoped topics.

---

## 11. Error model

Standard envelope:

```json
{
  "timestamp": "...",
  "path": "/api/v1/...",
  "code": "WHATSAPP_DELIVERY_FILTERED",
  "message": "Meta rejected delivery (#131049)",
  "details": { "metaCode": 131049 },
  "traceId": "..."
}
```

Map Meta codes (`131047`, `131049`, `132001`, `190`) to stable platform codes for UI copy.
