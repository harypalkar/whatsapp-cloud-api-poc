# MASTER-01 — Database Design

## 1. Platform standards

| Rule | Specification |
|---|---|
| Engine | PostgreSQL 16+ |
| PK | UUID (`gen_random_uuid()`) |
| Soft delete | `deleted_at TIMESTAMPTZ NULL` |
| Audit | `created_at`, `updated_at`, `created_by`, `updated_by` where applicable |
| Optimistic lock | `version BIGINT NOT NULL DEFAULT 0` |
| Tenancy | `company_id UUID NOT NULL` on all tenant tables |
| Money | `NUMERIC(18,2)` + ISO currency |
| JSON | `JSONB` for flexible attributes / workflow graphs |
| Time | `TIMESTAMPTZ` everywhere |
| Migrations | Flyway only (`V{n}__description.sql`) |

---

## 2. Complete schema catalog

### 2.1 Platform / identity

| Table | Purpose |
|---|---|
| `companies` | Tenant root |
| `users` | Portal users (agents/admins) |
| `roles` | Per-tenant roles |
| `permissions` | Global permission codes |
| `role_permissions` | Role → permission |
| `user_roles` | User → role |
| `refresh_tokens` | Hashed refresh tokens + family |
| `email_verifications` | Signup / change-email tokens |
| `password_reset_tokens` | Reset flow |
| `api_keys` | Tenant machine credentials (hashed) |

### 2.2 Billing

| Table | Purpose |
|---|---|
| `plans` | Product catalog |
| `subscriptions` | Company plan state |
| `invoices` | Billing documents |
| `payment_events` | Provider webhook idempotency |
| `usage_counters` | Monthly message/agent usage |

### 2.3 Meta / WhatsApp

| Table | Purpose |
|---|---|
| `whatsapp_accounts` | Connected numbers + encrypted tokens |
| `whatsapp_templates` | Synced template catalog |
| `webhook_events` | Raw webhook store + process state |
| `meta_oauth_states` | Embedded Signup CSRF/state |

### 2.4 CRM

| Table | Purpose |
|---|---|
| `customers` | Contacts |
| `customer_tags` | Tag dictionary |
| `customer_tag_map` | M2M |
| `customer_import_jobs` | CSV import batches |
| `customer_groups` | Segments |
| `customer_group_members` | Segment membership |

### 2.5 Media / campaigns

| Table | Purpose |
|---|---|
| `media_assets` | MinIO object metadata |
| `campaigns` | Campaign header |
| `campaign_media` | Attachments |
| `campaign_recipients` | Per-recipient state machine |
| `campaign_stats_daily` | Rollup (projection) |

### 2.6 Conversations / agents

| Table | Purpose |
|---|---|
| `conversations` | Threads |
| `messages` | In/out messages |
| `message_media` | Message ↔ media |
| `conversation_events` | Assign/transfer/close timeline |
| `agent_presence` | Online/away (or Redis-only) |

### 2.7 Forms / workflows / notify / audit

| Table | Purpose |
|---|---|
| `forms` | Form definitions |
| `form_fields` | Field schema |
| `form_responses` | Submissions |
| `workflows` | Automation graphs |
| `workflow_runs` | Execution instances |
| `notifications` | In-app notifications |
| `audit_logs` | Append-only audit |
| `outbox_events` | Transactional outbox |

---

## 3. Critical indexes

| Table | Index |
|---|---|
| `users` | `UNIQUE (company_id, email) WHERE deleted_at IS NULL` |
| `customers` | `UNIQUE (company_id, mobile_e164) WHERE deleted_at IS NULL` |
| `whatsapp_accounts` | `UNIQUE (phone_number_id)`, `UNIQUE (company_id, phone_number_id)` |
| `messages` | `UNIQUE (wa_message_id) WHERE wa_message_id IS NOT NULL` |
| `messages` | `(company_id, conversation_id, created_at DESC)` |
| `conversations` | `(company_id, status, updated_at DESC)` |
| `campaign_recipients` | `(campaign_id, status)`, `(company_id, wa_message_id)` |
| `webhook_events` | `UNIQUE (external_id)` or hash of payload id |
| `outbox_events` | `(published_at NULLS FIRST, created_at)` |
| `audit_logs` | `(company_id, created_at DESC)` |

---

## 4. Soft delete & locking

- Reads default to `deleted_at IS NULL` (Hibernate `@SQLRestriction` or Spec).
- Updates increment `version`; conflict → `409 OptimisticLock`.
- Hard delete only for GDPR erase jobs (explicit, audited).

---

## 5. Flyway plan (implementation in later MASTER)

```
V1__platform_companies_users_rbac.sql
V2__auth_tokens_api_keys.sql
V3__billing_plans_subscriptions.sql
V4__whatsapp_accounts_templates.sql
V5__customers_crm.sql
V6__media_assets.sql
V7__campaigns.sql
V8__conversations_messages.sql
V9__forms_workflows.sql
V10__notifications_audit_outbox.sql
V11__seed_platform_plans_permissions.sql
```

**No SQL files are created in MASTER-01.**

---

## 6. Sharding readiness

| Phase | Strategy |
|---|---|
| 0–50k tenants | Single PG cluster, strong indexes |
| 50k+ | Citus / schema-per-shard by `company_id` hash |
| Hot paths | Partition `messages`, `webhook_events`, `audit_logs` by month |

Application always includes `company_id` in queries → shard key ready.

---

## 7. Data retention

| Data | Policy (default) |
|---|---|
| Webhook raw payloads | 30–90 days |
| Audit logs | 1–7 years (compliance config) |
| Campaign recipients | Life of company + export |
| Media | Soft delete + lifecycle to cold storage |
