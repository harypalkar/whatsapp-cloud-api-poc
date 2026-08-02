# MASTER-05 — Investor Pitch Architecture

## One-liner

WhatsFlow is a multi-tenant WhatsApp engagement OS — campaigns, inbox, automation, AI/RAG, payments, and white-label — built for India-first scale and global enterprise readiness.

## Why now

- WhatsApp is the default business channel in India  
- SMBs need no-code Interakt/WATI-class tools with AI differentiation  
- Meta Embedded Signup unlocks self-serve WABA onboarding  

## Moat layers

1. **Tenant-safe modular monolith** → fast ship, microservice extract later  
2. **AI provider abstraction** → not locked to one LLM vendor  
3. **RAG grounded on business docs** → higher conversion vs generic bots  
4. **India payments + GST + DPDP** → local GTM advantage  
5. **Marketplace** → ecosystem lock-in  

## Architecture snapshot

```
Angular portals → Nginx → Spring Boot modules → PG/Redis/MinIO/Kafka
                              ↓
                     Meta Cloud API + LLM providers + Payment rails
```

## Unit economics levers

- Subscription tiers + message overage  
- AI token pass-through / markup  
- Marketplace revenue share  
- White-label enterprise contracts  

## Scale story

Designed for 100k companies, 10M customers, 100M msgs/day via horizontal API/workers, Redis/Kafka async, and shard-ready `tenant_id`.
