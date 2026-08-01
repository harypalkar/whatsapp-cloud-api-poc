--liquibase formatted sql

--changeset whatsapp:002-create-webhook-events-table
CREATE TABLE webhook_events (
    id           BIGSERIAL PRIMARY KEY,
    event_object VARCHAR(100),
    payload      TEXT         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_webhook_events_created_at ON webhook_events (created_at);

--rollback DROP TABLE IF EXISTS webhook_events;
