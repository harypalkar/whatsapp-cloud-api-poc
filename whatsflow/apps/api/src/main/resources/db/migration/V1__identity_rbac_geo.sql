-- V1: Identity, RBAC, geo reference data
-- Note: On PostgreSQL, enable pgcrypto if needed: CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- H2 (local profile) provides gen_random_uuid() in PostgreSQL compatibility mode.

-- ---------------------------------------------------------------------------
-- Geo (global reference — no tenant_id)
-- ---------------------------------------------------------------------------
CREATE TABLE countries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(3)  NOT NULL,
    name            VARCHAR(128) NOT NULL,
    created_by      UUID,
    created_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by     UUID,
    modified_date   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_countries_code UNIQUE (code)
);

CREATE TABLE states (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id      UUID NOT NULL REFERENCES countries(id),
    code            VARCHAR(16) NOT NULL,
    name            VARCHAR(128) NOT NULL,
    created_by      UUID,
    created_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by     UUID,
    modified_date   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_states_country_code UNIQUE (country_id, code)
);

CREATE TABLE cities (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    state_id        UUID NOT NULL REFERENCES states(id),
    name            VARCHAR(128) NOT NULL,
    created_by      UUID,
    created_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by     UUID,
    modified_date   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_cities_state_id ON cities(state_id);

-- ---------------------------------------------------------------------------
-- Tenant root
-- ---------------------------------------------------------------------------
CREATE TABLE companies (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(256) NOT NULL,
    slug            VARCHAR(128) NOT NULL,
    status          VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    timezone        VARCHAR(64)  NOT NULL DEFAULT 'UTC',
    created_by      UUID,
    created_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by     UUID,
    modified_date   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_companies_slug UNIQUE (slug)
);

CREATE INDEX idx_companies_status ON companies(status) WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- Permissions (global catalog)
-- ---------------------------------------------------------------------------
CREATE TABLE permissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(128) NOT NULL,
    description     VARCHAR(512),
    module          VARCHAR(64),
    created_by      UUID,
    created_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by     UUID,
    modified_date   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_permissions_code UNIQUE (code)
);

-- ---------------------------------------------------------------------------
-- Users & RBAC (tenant-scoped)
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES companies(id),
    email           VARCHAR(320) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(256) NOT NULL,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    role_hint       VARCHAR(64),
    created_by      UUID,
    created_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by     UUID,
    modified_date   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_users_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX idx_users_tenant_id ON users(tenant_id) WHERE deleted = FALSE;

CREATE TABLE roles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES companies(id),
    code            VARCHAR(64)  NOT NULL,
    name            VARCHAR(128) NOT NULL,
    description     VARCHAR(512),
    system_role     BOOLEAN NOT NULL DEFAULT FALSE,
    created_by      UUID,
    created_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by     UUID,
    modified_date   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_roles_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX idx_roles_tenant_id ON roles(tenant_id) WHERE deleted = FALSE;

CREATE TABLE role_permissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id         UUID NOT NULL REFERENCES roles(id),
    permission_id   UUID NOT NULL REFERENCES permissions(id),
    created_by      UUID,
    created_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by     UUID,
    modified_date   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_role_permissions UNIQUE (role_id, permission_id)
);

CREATE TABLE user_roles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES companies(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    role_id         UUID NOT NULL REFERENCES roles(id),
    created_by      UUID,
    created_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by     UUID,
    modified_date   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_user_roles UNIQUE (tenant_id, user_id, role_id)
);

CREATE INDEX idx_user_roles_tenant_user ON user_roles(tenant_id, user_id) WHERE deleted = FALSE;

CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES companies(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    token_hash      VARCHAR(128) NOT NULL,
    family_id       UUID NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked         BOOLEAN NOT NULL DEFAULT FALSE,
    created_by      UUID,
    created_date    TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by     UUID,
    modified_date   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(tenant_id, user_id) WHERE deleted = FALSE AND revoked = FALSE;
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash) WHERE deleted = FALSE;
