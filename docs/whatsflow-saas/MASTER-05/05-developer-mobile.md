# MASTER-05 — Developer Portal & Mobile APIs

## Public API

- REST under `/api/v1/public/**` and `/api/v1/developer/**`
- GraphQL at `/graphql` (schema-first stubs)
- API keys (hashed), OAuth2 client credentials ready
- Rate limits: Redis sliding window per key

## Mobile

Unified mobile BFF: `/api/v1/mobile/**`  
Push: FCM / APNs abstraction (`PushNotificationProvider`)  
Auth: JWT + refresh; device registration table `mobile_devices`
