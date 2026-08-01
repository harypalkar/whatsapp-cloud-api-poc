--liquibase formatted sql

--changeset whatsapp:001-create-conversations-table
CREATE TABLE conversations (
    id         BIGSERIAL PRIMARY KEY,
    mobile     VARCHAR(20)  NOT NULL,
    message    TEXT         NOT NULL,
    direction  VARCHAR(10)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--rollback DROP TABLE IF EXISTS conversations;
