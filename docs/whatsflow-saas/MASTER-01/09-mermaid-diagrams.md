# MASTER-01 — Mermaid Diagrams Pack

Central diagram index. (C4 / ER also live in their own docs.)

---

## 1. Self-serve onboarding sequence

```mermaid
sequenceDiagram
  actor Admin as CompanyAdmin
  participant Web as AngularWeb
  participant API as WhatsFlowAPI
  participant Mail as EmailProvider
  participant Meta as MetaEmbeddedSignup
  participant Graph as WhatsAppCloudAPI

  Admin->>Web: Register
  Web->>API: POST /auth/register
  API->>Mail: Verification email
  Admin->>Web: Verify email
  Web->>API: POST /auth/verify-email
  Admin->>Web: Choose plan
  Web->>API: POST /subscriptions
  Admin->>Web: Connect WhatsApp
  Web->>API: POST /meta/embedded-signup/start
  API-->>Web: Launch config
  Web->>Meta: Embedded Signup UI
  Meta-->>Web: Authorization response
  Web->>API: POST /meta/embedded-signup/complete
  API->>Graph: Exchange + fetch phone/WABA
  API-->>Web: Connected account
  Admin->>Web: Import customers / first campaign
```

---

## 2. Campaign send + status

```mermaid
sequenceDiagram
  participant API as API
  participant Q as RedisStream
  participant W as Worker
  participant Meta as CloudAPI
  participant WH as WebhookEndpoint

  API->>API: Create/Schedule campaign
  API->>Q: Enqueue batches
  W->>Q: Claim job
  W->>Meta: POST template message
  Meta-->>W: accepted + wamid
  W->>W: recipient=ACCEPTED
  Meta->>WH: status sent/delivered/failed
  WH->>API: Persist status + Meta errors
```

---

## 3. Inbound message → inbox

```mermaid
flowchart LR
  WA[Customer_WhatsApp] --> Meta[Meta_Cloud]
  Meta --> WH[Webhook_Controller]
  WH --> Idem[Idempotent_Store]
  Idem --> Conv[Open_or_Create_Conversation]
  Conv --> CRM[Upsert_Customer]
  Conv --> Assign[Routing_Rules]
  Assign --> WS[WebSocket_Fanout]
  WS --> AgentUI[Agent_Inbox]
```

---

## 4. Tenant isolation

```mermaid
flowchart TB
  Req[HTTP_Request] --> JWT[JWT_Filter]
  JWT --> TC[TenantContext_companyId]
  TC --> UC[UseCase]
  UC --> Repo[Repository]
  Repo --> SQL["SQL WHERE company_id = :ctx"]
  SQL --> PG[(PostgreSQL)]
```

---

## 5. Modular monolith → microservice extraction path

```mermaid
flowchart LR
  subgraph today [Today]
    MM[Modular_Monolith]
  end
  subgraph later [Later]
    CampSvc[Campaign_Service]
    InboxSvc[Inbox_Service]
    CoreSvc[Identity_Tenant_Billing]
  end
  MM -->|extract via ports_events| CampSvc
  MM -->|extract via ports_events| InboxSvc
  MM --> CoreSvc
```

---

## 6. State machines

### Campaign

```mermaid
stateDiagram-v2
  [*] --> DRAFT
  DRAFT --> SCHEDULED
  SCHEDULED --> RUNNING
  RUNNING --> PAUSED
  PAUSED --> RUNNING
  RUNNING --> COMPLETED
  RUNNING --> CANCELLED
  SCHEDULED --> CANCELLED
```

### Campaign recipient

```mermaid
stateDiagram-v2
  [*] --> PENDING
  PENDING --> ACCEPTED
  PENDING --> FAILED
  ACCEPTED --> SENT
  SENT --> DELIVERED
  SENT --> FAILED
  DELIVERED --> READ
```

### Conversation

```mermaid
stateDiagram-v2
  [*] --> OPEN
  OPEN --> ASSIGNED
  ASSIGNED --> PENDING_CUSTOMER
  PENDING_CUSTOMER --> ASSIGNED
  ASSIGNED --> CLOSED
  OPEN --> CLOSED
```

---

## 7. Deployment

```mermaid
flowchart TB
  subgraph edge [Edge]
    N[Nginx]
  end
  subgraph app [App_Tier]
    A1[API_1]
    A2[API_2]
    W1[Worker_1]
    W2[Worker_2]
  end
  subgraph data [Data]
    PG[(PostgreSQL)]
    R[(Redis)]
    M[(MinIO)]
  end
  subgraph obs [Obs]
    P[Prometheus]
    G[Grafana]
    E[ELK]
  end
  N --> A1
  N --> A2
  A1 --> PG
  A2 --> PG
  A1 --> R
  W1 --> R
  W1 --> PG
  A1 --> M
  A1 --> P
  W1 --> P
  P --> G
  A1 --> E
```
