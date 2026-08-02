# MASTER-05 — CRM, Marketing & Workflow

## Sales CRM

Pipeline stages: NEW → CONTACTED → QUALIFIED → PROPOSAL → WON / LOST  
Entities: `leads`, `opportunities`, `quotations`, `follow_ups`, `tasks`, `calendar_events`, `crm_notes`, `customer_journeys`

## Marketing

- Campaign A/B variants (`campaign_variants`)
- Segments (`customer_segments` + rules JSON)
- Coupons / discount codes
- QR codes → landing pages
- Referral program (`referrals`, `affiliates`)

## Workflow builder (advanced)

Nodes: START, DELAY, CONDITION, API, WEBHOOK, WHATSAPP, EMAIL, SMS, PAYMENT, AI, END  
Persisted as JSON graph; engine executes with idempotent `workflow_runs` / `workflow_run_steps`.
