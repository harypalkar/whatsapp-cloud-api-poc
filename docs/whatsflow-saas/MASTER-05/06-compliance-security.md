# MASTER-05 — Compliance & Security

## India + Global

| Framework | Controls |
|---|---|
| DPDP Act (India) | Consent records, purpose limitation, erase/export |
| GDPR | Data subject rights APIs, DPA templates |
| Audit | Append-only `audit_logs` + AI invocation logs |
| Consent | Opt-in/out WhatsApp + marketing + cookies |

## Auth upgrades

MFA (TOTP), OTP login, Google/Microsoft OAuth2 login hooks  

## Encryption

- Tokens / AI keys / payment secrets: AES-GCM  
- TLS everywhere at edge  
- Field-level optional for PII  

## Policies

Privacy · Terms · Cookie — versioned `legal_documents` + acceptance timestamps  
