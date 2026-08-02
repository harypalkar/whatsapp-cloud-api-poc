CREATE TABLE campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    whatsapp_account_id UUID,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    template_name VARCHAR(255),
    language VARCHAR(16) DEFAULT 'en',
    promo_code VARCHAR(64),
    scheduled_at TIMESTAMPTZ,
    recurring_cron VARCHAR(64),
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE campaign_recipients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    wa_message_id VARCHAR(128),
    error_code VARCHAR(32),
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_camp_recip ON campaign_recipients (campaign_id, status);

CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id),
    assigned_user_id UUID,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    unread_count INT NOT NULL DEFAULT 0,
    last_message_preview VARCHAR(500),
    last_customer_message_at TIMESTAMPTZ,
    window_expires_at TIMESTAMPTZ,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_conv_tenant ON conversations (tenant_id, status);

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    conversation_id UUID NOT NULL REFERENCES conversations(id),
    direction VARCHAR(16) NOT NULL,
    type VARCHAR(32) NOT NULL DEFAULT 'text',
    body TEXT,
    wa_message_id VARCHAR(128),
    delivery_status VARCHAR(32),
    meta_errors_json TEXT,
    media_url VARCHAR(1024),
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_msg_conv ON messages (conversation_id, created_date);

CREATE TABLE conversation_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    conversation_id UUID NOT NULL REFERENCES conversations(id),
    author_user_id UUID,
    note TEXT NOT NULL,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE webhook_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    external_id VARCHAR(128),
    event_type VARCHAR(64),
    payload_json TEXT NOT NULL,
    process_status VARCHAR(32) NOT NULL DEFAULT 'RECEIVED',
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
