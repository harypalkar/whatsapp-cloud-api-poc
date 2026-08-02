# MASTER-05 — Checklists

## Production checklist

- [ ] All Flyway migrations applied on prod PG (incl. pgvector if enabled)
- [ ] AI provider keys in secret store (not `.env` in images)
- [ ] Payment webhooks verified (signature)
- [ ] Tenant isolation tests green
- [ ] Rate limits + WAF at edge
- [ ] Backups + restore drill passed
- [ ] Observability: API, Meta errors, AI cost, payment failures
- [ ] Legal docs published (Privacy, Terms, Cookie, DPDP notice)

## Go-live checklist

- [ ] Custom domains / TLS
- [ ] Meta Embedded Signup app reviewed
- [ ] Marketing templates approved per pilot tenants
- [ ] Support ticket + AI support bot enabled
- [ ] Status page + on-call runbook
- [ ] Soft launch with 5–10 pilot companies

## Business launch checklist

- [ ] Pricing page live (Starter → Enterprise)
- [ ] Demo booking + sales pipeline
- [ ] Affiliate / referral terms
- [ ] GST invoicing validated (India)
- [ ] Marketplace seed listings
- [ ] Hindi + English UI strings
- [ ] Press kit / investor one-pager
