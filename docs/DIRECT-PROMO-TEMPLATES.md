# Direct promo to ANY mobile (no customer "Hi" required)

## Why Bharat needed to say Hi

WhatsApp rule:

| Message type | Can send without prior customer reply? |
|---|---|
| Free text / CTA / buttons | **NO** (only inside 24-hour window) |
| Approved **MARKETING template** | **YES** (cold outreach) |

That is why `7718884343` failed with `#131047` until he messaged first.

For numbers like `7718986249`, you must use a **marketing template**.

## Step 1 — Create template in Meta (one-time)

1. Open [Meta Business Suite](https://business.facebook.com/) → **WhatsApp Manager** → **Message templates**
   or Developers → WhatsApp → **Message templates**
2. **Create template**
3. Fill:

| Field | Value |
|---|---|
| Name | `altitude_welcome_promo` |
| Category | **Marketing** |
| Language | English (`en`) |
| Body | see below |
| Button | **Visit website** → `https://www.altitudelabs.in/` |

### Recommended body

```text
Hello {{1}},

Welcome to Altitude Labs™

Pure Himalayan Shilajit — lab-tested, altitude-harvested.
Boost energy, strength, stamina and recovery.

Use Promo Code: {{2}}
Get ₹100 OFF on your first order.

Tap below to shop.
```

Variables:

- `{{1}}` = customer name (e.g. Amit)
- `{{2}}` = promo code (e.g. WELCOME100)

4. Submit for approval and wait until status = **Approved**

## Step 2 — Configure app

In `.env` / `application.yml`:

```env
WHATSAPP_PHONE_NUMBER_ID=1226308087231072
WHATSAPP_TEMPLATE_NAME=altitude_welcome_promo
WHATSAPP_TEMPLATE_LANGUAGE=en
WHATSAPP_API_VERSION=v23.0
```

```yaml
whatsapp:
  access-token: ${WHATSAPP_TOKEN:}
  phone-number-id: ${WHATSAPP_PHONE_NUMBER_ID:1226308087231072}
  template-name: altitude_welcome_promo
  template-language: en
```

## Step 3 — Send direct promo (no Hi needed)

Primary API:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/messages/send-template `
  -ContentType "application/json" `
  -Headers @{ "X-Company-Id" = "1" } `
  -Body '{
    "mobile":"917506426501",
    "customerName":"Harish",
    "promoCode":"WELCOME100"
  }'
```

Onboard helper:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/customers/onboard `
  -ContentType "application/json" `
  -Body '{
    "mobile":"7718986249",
    "name":"Amit",
    "promoCode":"WELCOME100",
    "messageStyle":"template"
  }'
```

Bulk:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/customers/onboard/bulk `
  -ContentType "application/json" `
  -Body '{
    "customers":[
      {"mobile":"7718986249","name":"Amit","promoCode":"WELCOME100","messageStyle":"template"},
      {"mobile":"917718884343","name":"Bharat","promoCode":"WELCOME100","messageStyle":"template"}
    ]
  }'
```

Default `messageStyle` is now **`template`** (cold outreach).

## Still required in development mode

Add each recipient under Meta → WhatsApp → API Setup → **To** (OTP verify), until the business is fully live.
