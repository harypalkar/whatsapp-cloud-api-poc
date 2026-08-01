-- Standalone SQL script (same schema as Liquibase migration 001)
-- Run manually: psql -U postgres -d whatsapp_db -f conversations.sql

CREATE TABLE IF NOT EXISTS conversations (
    id         BIGSERIAL PRIMARY KEY,
    mobile     VARCHAR(20)  NOT NULL,
    message    TEXT         NOT NULL,
    direction  VARCHAR(10)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
