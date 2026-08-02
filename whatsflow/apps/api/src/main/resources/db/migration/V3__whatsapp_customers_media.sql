CREATE TABLE whatsapp_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    waba_id VARCHAR(64),
    phone_number_id VARCHAR(64) NOT NULL,
    business_id VARCHAR(64),
    display_phone VARCHAR(32),
    verified_name VARCHAR(255),
    access_token_enc BYTEA,
    webhook_verify_token VARCHAR(255),
    quality_rating VARCHAR(32),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_wa_phone UNIQUE (phone_number_id)
);

CREATE TABLE whatsapp_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    whatsapp_account_id UUID,
    meta_template_id VARCHAR(64),
    name VARCHAR(255) NOT NULL,
    language VARCHAR(16) NOT NULL DEFAULT 'en',
    category VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    body_param_count INT NOT NULL DEFAULT 0,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    mobile_e164 VARCHAR(32) NOT NULL,
    name VARCHAR(255),
    email VARCHAR(320),
    opted_in BOOLEAN NOT NULL DEFAULT TRUE,
    blacklisted BOOLEAN NOT NULL DEFAULT FALSE,
    attributes_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_customer_mobile UNIQUE (tenant_id, mobile_e164)
);
CREATE INDEX idx_customers_tenant ON customers (tenant_id);

CREATE TABLE customer_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(128) NOT NULL,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_tag_name UNIQUE (tenant_id, name)
);

CREATE TABLE customer_tag_map (
    customer_id UUID NOT NULL REFERENCES customers(id),
    tag_id UUID NOT NULL REFERENCES customer_tags(id),
    PRIMARY KEY (customer_id, tag_id)
);

CREATE TABLE customer_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(128) NOT NULL,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE customer_group_members (
    group_id UUID NOT NULL REFERENCES customer_groups(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    PRIMARY KEY (group_id, customer_id)
);

CREATE TABLE media_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    object_key VARCHAR(1024) NOT NULL,
    file_name VARCHAR(512),
    mime_type VARCHAR(128),
    size_bytes BIGINT,
    kind VARCHAR(32),
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
