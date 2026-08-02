# MASTER-01 — C4 Architecture

## Level 1 — System Context

```mermaid
C4Context
  title WhatsFlow System Context
  Person(admin, "Company Admin", "Registers, bills, connects WhatsApp")
  Person(agent, "Agent", "Handles shared inbox chats")
  Person(owner, "Platform Owner", "Operates SaaS control plane")
  Person(customer, "End Customer", "Chats on WhatsApp")
  System(whatsflow, "WhatsFlow SaaS", "Multi-tenant WhatsApp engagement platform")
  System_Ext(meta, "Meta WhatsApp Cloud", "Cloud API, webhooks, Embedded Signup")
  System_Ext(payments, "Payment Provider", "Subscriptions & invoices")
  System_Ext(email, "Email Provider", "Verification & notifications")

  Rel(admin, whatsflow, "Uses HTTPS")
  Rel(agent, whatsflow, "Uses HTTPS / WSS")
  Rel(owner, whatsflow, "Uses HTTPS")
  Rel(customer, meta, "WhatsApp messages")
  Rel(whatsflow, meta, "Send/receive Graph API")
  Rel(meta, whatsflow, "Webhooks")
  Rel(whatsflow, payments, "Billing webhooks")
  Rel(whatsflow, email, "Transactional email")
```

---

## Level 2 — Containers

```mermaid
C4Container
  title WhatsFlow Containers
  Person(user, "Business User / Agent")
  Container(web, "Angular Web", "Angular 20", "Admin, agent inbox, campaigns, forms")
  Container(api, "API", "Spring Boot 3.5", "REST, WS, webhooks, auth")
  Container(worker, "Worker", "Spring Boot", "Campaigns, webhook jobs, automation")
  ContainerDb(pg, "PostgreSQL", "RDBMS", "Tenant data, audit, outbox")
  ContainerDb(redis, "Redis", "Cache / Streams", "Sessions, queues, rate limits")
  ContainerDb(minio, "MinIO", "Object storage", "Images, video, PDF")
  System_Ext(meta, "Meta Cloud API")
  Container_Ext(nginx, "Nginx", "Edge", "TLS, static, reverse proxy")

  Rel(user, nginx, "HTTPS/WSS")
  Rel(nginx, web, "Static assets")
  Rel(nginx, api, "API / WS / webhooks")
  Rel(api, pg, "JDBC")
  Rel(api, redis, "Cache/queue")
  Rel(api, minio, "S3 API")
  Rel(api, meta, "Graph HTTPS")
  Rel(worker, pg, "JDBC")
  Rel(worker, redis, "Consume jobs")
  Rel(worker, minio, "Media")
  Rel(worker, meta, "Graph HTTPS")
  Rel(meta, nginx, "Webhook callbacks")
```

---

## Level 3 — API Components (modular monolith)

```mermaid
C4Component
  title WhatsFlow API Components
  Container_Boundary(api, "whatsflow-api") {
    Component(identity, "Identity Module", "AuthN/Z, JWT, users, roles")
    Component(tenant, "Tenant Module", "Company, settings, isolation")
    Component(billing, "Billing Module", "Plans, subscriptions, entitlements")
    Component(metaMod, "Meta Module", "Embedded Signup, Graph admin")
    Component(wa, "WhatsApp Module", "Provider ports, send/receive")
    Component(crm, "CRM Module", "Customers, segments, import")
    Component(campaign, "Campaign Module", "Campaigns, recipients")
    Component(inbox, "Inbox Module", "Conversations, assignment")
    Component(workflow, "Workflow Module", "Automations")
    Component(forms, "Forms Module", "Builder & responses")
    Component(analytics, "Analytics Module", "Reports, metrics queries")
    Component(audit, "Audit Module", "Security trail")
    Component(kernel, "Shared Kernel", "TenantContext, errors, events")
  }
  Rel(identity, kernel, "Uses")
  Rel(tenant, kernel, "Uses")
  Rel(wa, metaMod, "Credentials")
  Rel(campaign, wa, "Send template")
  Rel(inbox, wa, "Session send")
  Rel(workflow, inbox, "Actions")
  Rel(crm, campaign, "Audiences")
```

---

## Level 4 — Code (illustrative, not generated)

Example inbound webhook flow (classes — design only):

1. `MetaWhatsAppWebhookController` (api)  
2. `ProcessWhatsAppWebhookUseCase` (application)  
3. `WebhookEventRepository` (port.out)  
4. `ConversationDomainService` (domain)  
5. `JpaWebhookEventAdapter` / `MetaSignatureVerifier` (infrastructure)  
6. `InboxRealtimePublisher` → WebSocket fan-out  

---

## Deployment view

| Environment | Notes |
|---|---|
| `local` | Docker Compose: api, worker, pg, redis, minio, mailhog |
| `staging` | Single region; Meta test numbers / approved templates |
| `production` | Multi-AZ API/worker; managed PG/Redis; WAF at Nginx/CDN |

---

## Trust boundaries

1. Public internet → Nginx  
2. Meta webhooks → dedicated path, signature verification  
3. Tenant JWT → API application layer  
4. Platform admin → separate role + audit  
5. Secrets vault / env → never logged  
