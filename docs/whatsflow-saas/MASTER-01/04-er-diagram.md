# MASTER-01 — ER Diagram

## 1. Core tenancy & identity

```mermaid
erDiagram
  companies ||--o{ users : employs
  companies ||--o{ roles : defines
  companies ||--o{ api_keys : issues
  users ||--o{ user_roles : has
  roles ||--o{ user_roles : granted
  roles ||--o{ role_permissions : grants
  permissions ||--o{ role_permissions : mapped
  users ||--o{ refresh_tokens : owns
  companies ||--o{ email_verifications : verifies

  companies {
    uuid id PK
    string name
    string slug UK
    string status
    string timezone
    timestamptz created_at
    timestamptz updated_at
    timestamptz deleted_at
    bigint version
  }

  users {
    uuid id PK
    uuid company_id FK
    string email
    string password_hash
    string full_name
    boolean email_verified
    boolean enabled
    timestamptz deleted_at
    bigint version
  }

  roles {
    uuid id PK
    uuid company_id FK
    string code
    string name
  }

  permissions {
    uuid id PK
    string code UK
    string description
  }
```

## 2. Billing

```mermaid
erDiagram
  plans ||--o{ subscriptions : offered
  companies ||--o{ subscriptions : pays
  subscriptions ||--o{ invoices : bills
  subscriptions ||--o{ usage_counters : meters

  plans {
    uuid id PK
    string code UK
    string name
    int max_agents
    int max_messages_month
    jsonb features
  }

  subscriptions {
    uuid id PK
    uuid company_id FK
    uuid plan_id FK
    string status
    timestamptz current_period_end
  }
```

## 3. Meta / WhatsApp

```mermaid
erDiagram
  companies ||--o{ whatsapp_accounts : connects
  whatsapp_accounts ||--o{ whatsapp_templates : catalogs
  companies ||--o{ webhook_events : receives

  whatsapp_accounts {
    uuid id PK
    uuid company_id FK
    string waba_id
    string phone_number_id UK
    string business_id
    string display_phone
    string verified_name
    bytea access_token_enc
    string quality_rating
    string messaging_limit_tier
    string webhook_verify_token
    string status
  }

  whatsapp_templates {
    uuid id PK
    uuid company_id FK
    uuid whatsapp_account_id FK
    string meta_template_id
    string name
    string language
    string category
    string status
    int body_param_count
  }
```

## 4. CRM, media, campaigns

```mermaid
erDiagram
  companies ||--o{ customers : owns
  companies ||--o{ customer_tags : defines
  customers ||--o{ customer_tag_map : tagged
  customer_tags ||--o{ customer_tag_map : used
  companies ||--o{ media_assets : stores
  companies ||--o{ campaigns : runs
  campaigns ||--o{ campaign_recipients : targets
  customers ||--o{ campaign_recipients : receives
  campaigns ||--o{ campaign_media : attaches
  media_assets ||--o{ campaign_media : used

  customers {
    uuid id PK
    uuid company_id FK
    string mobile_e164
    string name
    string email
    boolean opted_in
    jsonb attributes
  }

  campaigns {
    uuid id PK
    uuid company_id FK
    uuid whatsapp_account_id FK
    string name
    string status
    string template_name
    string language
    string promo_code
    timestamptz scheduled_at
  }

  campaign_recipients {
    uuid id PK
    uuid campaign_id FK
    uuid customer_id FK
    uuid company_id FK
    string status
    string wa_message_id
    string error_code
  }

  media_assets {
    uuid id PK
    uuid company_id FK
    string object_key
    string mime_type
    bigint size_bytes
    string kind
  }
```

## 5. Conversations, agents, forms, workflows

```mermaid
erDiagram
  companies ||--o{ conversations : owns
  customers ||--o{ conversations : chats
  conversations ||--o{ messages : contains
  users ||--o{ conversations : assigned
  companies ||--o{ forms : builds
  forms ||--o{ form_fields : has
  forms ||--o{ form_responses : collects
  companies ||--o{ workflows : automates
  workflows ||--o{ workflow_runs : executes
  companies ||--o{ notifications : notifies
  companies ||--o{ audit_logs : records

  conversations {
    uuid id PK
    uuid company_id FK
    uuid customer_id FK
    uuid assigned_user_id FK
    string status
    timestamptz last_customer_message_at
    timestamptz window_expires_at
  }

  messages {
    uuid id PK
    uuid company_id FK
    uuid conversation_id FK
    string direction
    string type
    text body
    string wa_message_id
    string delivery_status
    jsonb meta_errors
  }

  forms {
    uuid id PK
    uuid company_id FK
    string name
    string public_token
    string status
  }

  workflows {
    uuid id PK
    uuid company_id FK
    string name
    string status
    jsonb definition
  }

  audit_logs {
    uuid id PK
    uuid company_id FK
    uuid actor_user_id
    string action
    string entity_type
    uuid entity_id
    jsonb metadata
    timestamptz created_at
  }
```

## 6. Isolation invariant

Every business table above (except global `plans`, `permissions`) includes **`company_id`** and is queried only inside tenant scope.
