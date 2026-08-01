--liquibase formatted sql

--changeset whatsapp:003-create-core-saas-tables
CREATE TABLE IF NOT EXISTS companies (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    status       VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customers (
    id           BIGSERIAL PRIMARY KEY,
    company_id   BIGINT REFERENCES companies (id),
    mobile       VARCHAR(20)  NOT NULL,
    name         VARCHAR(255),
    opted_in     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_customers_mobile UNIQUE (mobile)
);

CREATE TABLE IF NOT EXISTS messages (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT,
    customer_mobile VARCHAR(20)  NOT NULL,
    direction       VARCHAR(10)  NOT NULL,
    message_type    VARCHAR(50)  NOT NULL DEFAULT 'text',
    body            TEXT,
    wa_message_id   VARCHAR(256),
    delivery_status VARCHAR(50),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_messages_mobile_created ON messages (customer_mobile, created_at);

CREATE TABLE IF NOT EXISTS conversation (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT,
    customer_mobile VARCHAR(20)  NOT NULL,
    status          VARCHAR(50)  NOT NULL DEFAULT 'OPEN',
    last_message_at TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_conversation_mobile UNIQUE (customer_mobile)
);

CREATE TABLE IF NOT EXISTS campaigns (
    id           BIGSERIAL PRIMARY KEY,
    company_id   BIGINT,
    name         VARCHAR(255) NOT NULL,
    promo_code   VARCHAR(64),
    status       VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    body         TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS templates (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    language_code VARCHAR(20)  NOT NULL,
    status        VARCHAR(50)  NOT NULL DEFAULT 'APPROVED',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS media (
    id           BIGSERIAL PRIMARY KEY,
    object_key   VARCHAR(512) NOT NULL,
    content_type VARCHAR(128),
    url          VARCHAR(1024),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    action       VARCHAR(150) NOT NULL,
    details      TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO companies (name, status)
SELECT 'Altitude Labs', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE name = 'Altitude Labs');

INSERT INTO customers (company_id, mobile, name, opted_in)
SELECT c.id, '917506426501', 'Harish', TRUE
FROM companies c
WHERE c.name = 'Altitude Labs'
  AND NOT EXISTS (SELECT 1 FROM customers WHERE mobile = '917506426501');

--rollback DROP TABLE IF EXISTS audit_logs; DROP TABLE IF EXISTS media; DROP TABLE IF EXISTS templates; DROP TABLE IF EXISTS campaigns; DROP TABLE IF EXISTS conversation; DROP TABLE IF EXISTS messages; DROP TABLE IF EXISTS customers; DROP TABLE IF EXISTS companies;
