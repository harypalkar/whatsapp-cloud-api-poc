# Meta WhatsApp Webhook Setup (Altitude Labs)

## Why verification was failing

Meta called:

`https://altitudelabs.ngrok-free.app/webhook/whatsapp`

The app previously exposed only:

`http://localhost:8080/api/webhook`

Path mismatch → Meta error: **The callback URL or verify token couldn't be validated.**

## Correct endpoints now

| Method | Path | Purpose |
|---|---|---|
| GET/POST | `/webhook/whatsapp` | **Use this in Meta** (matches your ngrok URL) |
| GET/POST | `/api/v1/webhooks/meta/whatsapp` | Spec-compatible alias |
| GET/POST | `/api/webhook` | Legacy alias |
| GET | `/health` | Returns `Application Running` |

Verify token (must match Meta exactly):

`AltitudeLabs@2026`

GET success response body is **only** the `hub.challenge` string (plain text, no JSON).

---

## Meta Developer Console values

1. Open **WhatsApp → Configuration → Webhook**
2. Click **Edit**

| Field | Value |
|---|---|
| Callback URL | `https://altitudelabs.ngrok-free.app/webhook/whatsapp` |
| Verify token | `AltitudeLabs@2026` |

3. Click **Verify and save**
4. Subscribe to field: `messages`

---

## Ngrok

```bash
# Install ngrok, then expose local port 8080
ngrok http 8080
```

If you use a reserved domain:

```bash
ngrok http --domain=altitudelabs.ngrok-free.app 8080
```

Keep ngrok running while verifying in Meta.

Important:

- App must listen on `8080`
- Ngrok must forward to that process
- No extra `/api` prefix in the Meta callback URL for `/webhook/whatsapp`

---

## Curl — verification (GET)

```bash
curl -i "http://localhost:8080/webhook/whatsapp?hub.mode=subscribe&hub.verify_token=AltitudeLabs%402026&hub.challenge=1234567890"
```

Expected:

```
HTTP/1.1 200
Content-Type: text/plain;charset=UTF-8

1234567890
```

Public ngrok test:

```bash
curl -i "https://altitudelabs.ngrok-free.app/webhook/whatsapp?hub.mode=subscribe&hub.verify_token=AltitudeLabs%402026&hub.challenge=1234567890"
```

Wrong token (expect 403):

```bash
curl -i "http://localhost:8080/webhook/whatsapp?hub.mode=subscribe&hub.verify_token=wrong&hub.challenge=1234567890"
```

---

## Curl — inbound webhook (POST)

```bash
curl -i -X POST "http://localhost:8080/webhook/whatsapp" \
  -H "Content-Type: application/json" \
  -d "{
    \"object\": \"whatsapp_business_account\",
    \"entry\": [
      {
        \"id\": \"WABA_ID\",
        \"changes\": [
          {
            \"field\": \"messages\",
            \"value\": {
              \"messaging_product\": \"whatsapp\",
              \"metadata\": {
                \"display_phone_number\": \"919512618333\",
                \"phone_number_id\": \"PHONE_NUMBER_ID\"
              },
              \"contacts\": [
                {
                  \"profile\": { \"name\": \"Test User\" },
                  \"wa_id\": \"917506426501\"
                }
              ],
              \"messages\": [
                {
                  \"from\": \"917506426501\",
                  \"id\": \"wamid.e2e\",
                  \"timestamp\": \"1720000000\",
                  \"type\": \"text\",
                  \"text\": { \"body\": \"Interested\" }
                }
              ]
            }
          }
        ]
      }
    ]
  }"
```

Expected body:

```
EVENT_RECEIVED
```

---

## Env

```env
WHATSAPP_VERIFY_TOKEN=AltitudeLabs@2026
```

Default in `application.yml` is already `AltitudeLabs@2026` if the env var is unset.
