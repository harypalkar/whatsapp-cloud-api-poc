# Altitude Labs WhatsApp Cloud API — Architecture

## Overview

Multi-tenant Spring Boot service that sends WhatsApp messages via Meta Graph API (`v23.0`), receives webhooks, persists conversations in PostgreSQL, and exposes a live chat dashboard.

## Mermaid — System architecture

```mermaid
flowchart TB
    subgraph Clients
        API[REST Clients / Postman]
        Dash[Live Chat Dashboard]
        MetaUI[Meta Cloud Webhooks]
    end

    subgraph App["Spring Boot API :8080"]
        TF[TenantFilter X-Company-Id]
        MC[MessageV1Controller]
        WC[MetaWhatsAppWebhookController]
        OC[CustomerOnboardController]
        SVC[WhatsAppService]
        CR[WhatsAppCredentialResolver]
        CS[ConversationService]
        WH[Webhook / MetaWebhookService]
    end

    subgraph Data
        PG[(PostgreSQL)]
    end

    subgraph Meta["Meta Graph API"]
        G["POST /v23.0/{phone-number-id}/messages"]
    end

    API --> TF --> MC --> SVC
    API --> OC --> SVC
    Dash --> CS
    MetaUI --> WC --> WH --> CS
    SVC --> CR
    CR --> PG
    SVC --> G
    SVC --> CS --> PG
    WH --> PG
```

## Mermaid — Send template sequence

```mermaid
sequenceDiagram
    participant C as Client
    participant API as MessageV1Controller
    participant S as WhatsAppService
    participant R as CredentialResolver
    participant M as Meta Graph v23.0
    participant DB as PostgreSQL

    C->>API: POST /api/v1/messages/send-template
    Note over C,API: X-Company-Id optional
    API->>S: sendPromoTemplate(mobile, name, promo)
    S->>R: resolve()
    R-->>S: token, phoneNumberId, template
    S->>M: POST .../messages type=template
    M-->>S: wamid...
    S->>DB: messages + conversations
    S-->>API: meta response
    API-->>C: status SENT
```

## Multi-tenant model

| Layer | Mechanism |
|---|---|
| Identity | `X-Company-Id` header (default `1` = Altitude Labs) |
| Credentials | `company_whatsapp_config` per company, fallback to `application.yml` |
| Data | `company_id` on customers, messages, conversation |

Unlimited companies: insert into `companies` + `company_whatsapp_config` with that tenant’s Meta token / phone number ID / template.

## Key endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/messages/send-template` | Marketing template cold send |
| POST | `/api/v1/messages/send` | text / template / image / pdf / video / location / buttons |
| GET/POST | `/api/v1/webhooks/meta/whatsapp` | Meta webhook verify + events |
| GET/POST | `/webhook/whatsapp` | Same webhook (ngrok-friendly) |
| GET | `/api/v1/conversations/{mobile}` | Chat timeline |
| GET | `/swagger-ui.html` | OpenAPI UI |
