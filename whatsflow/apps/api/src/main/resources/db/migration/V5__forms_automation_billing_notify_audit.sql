CREATE TABLE forms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    form_type VARCHAR(64),
    public_token VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    schema_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE form_fields (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    form_id UUID NOT NULL REFERENCES forms(id),
    field_key VARCHAR(64) NOT NULL,
    label VARCHAR(255) NOT NULL,
    field_type VARCHAR(32) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    options_json TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE form_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    form_id UUID NOT NULL REFERENCES forms(id),
    payload_json TEXT NOT NULL,
    customer_id UUID,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE automations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    trigger_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    definition_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE automation_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    automation_id UUID NOT NULL REFERENCES automations(id),
    node_key VARCHAR(64) NOT NULL,
    node_type VARCHAR(64) NOT NULL,
    config_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    max_agents INT,
    max_messages_month INT,
    price_monthly NUMERIC(18,2) DEFAULT 0,
    features_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    plan_id UUID NOT NULL REFERENCES plans(id),
    status VARCHAR(32) NOT NULL DEFAULT 'TRIAL',
    current_period_end TIMESTAMPTZ,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    subscription_id UUID,
    invoice_number VARCHAR(64) NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    status VARCHAR(32) NOT NULL DEFAULT 'DUE',
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    invoice_id UUID,
    provider VARCHAR(32),
    external_id VARCHAR(128),
    amount NUMERIC(18,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID,
    channel VARCHAR(32) NOT NULL,
    title VARCHAR(255),
    body TEXT,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    actor_user_id UUID,
    action VARCHAR(128) NOT NULL,
    entity_type VARCHAR(64),
    entity_id UUID,
    metadata_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

INSERT INTO plans (id, code, name, max_agents, max_messages_month, price_monthly)
SELECT gen_random_uuid(), 'STARTER', 'Starter', 3, 5000, 999
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE code = 'STARTER');
INSERT INTO plans (id, code, name, max_agents, max_messages_month, price_monthly)
SELECT gen_random_uuid(), 'GROWTH', 'Growth', 10, 50000, 4999
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE code = 'GROWTH');
INSERT INTO plans (id, code, name, max_agents, max_messages_month, price_monthly)
SELECT gen_random_uuid(), 'PROFESSIONAL', 'Professional', 50, 250000, 14999
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE code = 'PROFESSIONAL');
INSERT INTO plans (id, code, name, max_agents, max_messages_month, price_monthly)
SELECT gen_random_uuid(), 'ENTERPRISE', 'Enterprise', 500, 10000000, 49999
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE code = 'ENTERPRISE');
