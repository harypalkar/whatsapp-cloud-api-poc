CREATE TABLE IF NOT EXISTS ai_invocation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    provider_id VARCHAR(64) NOT NULL,
    capability VARCHAR(64) NOT NULL,
    model VARCHAR(128),
    success BOOLEAN NOT NULL DEFAULT TRUE,
    latency_ms INT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS knowledge_bases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
