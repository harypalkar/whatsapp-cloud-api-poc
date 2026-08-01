# Priority 1 — Bulk promo to many numbers

Website comes **after** this works.

## Blocker right now

Your Meta template `altitude_welcome_promo` is **In Review**.  
Until status = **Approved**, Meta returns `#132001` and bulk send cannot deliver.

## When Approved — send bulk

`POST /api/v1/messages/send-template/bulk`

### Simple (mobiles only)

```json
{
  "promoCode": "WELCOME100",
  "defaultCustomerName": "Customer",
  "mobiles": [
    "917718986249",
    "917506426501",
    "917718884343"
  ]
}
```

### Named recipients

```json
{
  "promoCode": "WELCOME100",
  "recipients": [
    { "mobile": "917718986249", "customerName": "Amit" },
    { "mobile": "917506426501", "customerName": "Harish" },
    { "mobile": "917718884343", "customerName": "Bharat" }
  ]
}
```

### PowerShell

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/messages/send-template/bulk `
  -ContentType "application/json" `
  -Body '{
    "promoCode":"WELCOME100",
    "mobiles":["917718986249","917506426501","917718884343"]
  }'
```

## Dev mode allowlist

Until WhatsApp is live, add each number in Meta → WhatsApp → API Setup → **To** (OTP).

## Priority 2 (later)

BhashSMS-style marketing website + Book a Demo.
