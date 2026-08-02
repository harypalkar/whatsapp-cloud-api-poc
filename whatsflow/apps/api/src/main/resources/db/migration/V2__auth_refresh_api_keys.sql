CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_refresh_user ON refresh_tokens (tenant_id, user_id);

CREATE TABLE email_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
