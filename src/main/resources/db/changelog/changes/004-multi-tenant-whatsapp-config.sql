--liquibase formatted sql

--changeset whatsapp:004-multi-tenant-whatsapp-config
CREATE TABLE IF NOT EXISTS company_whatsapp_config (
    id                   BIGSERIAL PRIMARY KEY,
    company_id           BIGINT       NOT NULL REFERENCES companies (id),
    access_token         TEXT,
    phone_number_id      VARCHAR(64),
    business_account_id  VARCHAR(64),
    verify_token         VARCHAR(255),
    template_name        VARCHAR(255) NOT NULL DEFAULT 'altitude_welcome_promo',
    template_language    VARCHAR(20)  NOT NULL DEFAULT 'en',
    api_version          VARCHAR(20)  NOT NULL DEFAULT 'v23.0',
    display_phone_number VARCHAR(32),
    active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_company_whatsapp_config_company UNIQUE (company_id)
);

--changeset whatsapp:004-add-read-status dbms:postgresql,h2
ALTER TABLE messages ADD COLUMN IF NOT EXISTS read_status VARCHAR(50);

--changeset whatsapp:004-indexes
CREATE INDEX IF NOT EXISTS idx_messages_company_created ON messages (company_id, created_at);
CREATE INDEX IF NOT EXISTS idx_customers_company ON customers (company_id);

--changeset whatsapp:004-seed-altitude-config
INSERT INTO company_whatsapp_config (
    company_id, template_name, template_language, api_version, display_phone_number, active
)
SELECT c.id, '3p_direct_integration_test_template', 'en', 'v23.0', '919512618333', TRUE
FROM companies c
WHERE c.name = 'Altitude Labs'
  AND NOT EXISTS (
      SELECT 1 FROM company_whatsapp_config w WHERE w.company_id = c.id
  );
