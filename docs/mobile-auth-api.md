# Mobile + Web Auth APIs (KaratKart onboarding)

APIs live in **identity-service** (port **8081**). Existing marketplace APIs are unchanged.

## OTP delivery (local)

**SMS is disabled by default.** You will **not** receive a text on the phone.

Local config: `marketplace.auth.otp.sms-enabled=false` and `dev-expose-otp=true`.

Read the OTP from the API response field `data.otp`, then call verify.

```powershell
$r = Invoke-RestMethod http://localhost:8081/api/v1/auth/otp/send -Method POST -ContentType "application/json" -Body '{"countryCode":"+91","mobileNumber":"7506426501"}'
$r.data.otp
$r.data.sessionId
```

Gateway base (StripPrefix=2):

```
http://localhost:9080/api/identity/api/v1/auth/...
```

Direct service base:

```
http://localhost:8081/api/v1/auth/...
```

## KaratKart screen → API map

| App screen | API |
|---|---|
| Phone / SIM → send code | `POST /api/v1/auth/otp/send` |
| Resend OTP | `POST /api/v1/auth/otp/resend` |
| Verify 6-digit OTP | `POST /api/v1/auth/otp/verify` |
| Select Individual / Business | `POST /api/v1/auth/user/type` |
| Personal or business details | `POST /api/v1/auth/user/details` |
| Create Secure PIN | `POST /api/v1/auth/pin/create` |
| Enter PIN (login) | `POST /api/v1/auth/pin/verify` |
| Web QR login | `POST /api/v1/auth/qr/create` (+ poll / confirm) |

## Mobile flow (curl)

```bash
# 1) Send OTP
curl -s -X POST http://localhost:8081/api/v1/auth/otp/send \
  -H "Content-Type: application/json" \
  -d '{"countryCode":"+91","mobileNumber":"9876543210"}'

# 2) Resend OTP
curl -s -X POST http://localhost:8081/api/v1/auth/otp/resend \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"<sessionId>"}'

# 3) Verify OTP (use otp from response when AUTH_DEV_EXPOSE_OTP=true)
curl -s -X POST http://localhost:8081/api/v1/auth/otp/verify \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"<sessionId>","otp":"482913"}'

# 4) Set user type
curl -s -X POST http://localhost:8081/api/v1/auth/user/type \
  -H "Content-Type: application/json" \
  -d '{"verificationToken":"<token>","userType":"INDIVIDUAL"}'

# 5a) Individual details
curl -s -X POST http://localhost:8081/api/v1/auth/user/details \
  -H "Content-Type: application/json" \
  -d '{"verificationToken":"<token>","fullName":"Ada Lovelace","email":"ada@example.com"}'

# 5b) Business details
curl -s -X POST http://localhost:8081/api/v1/auth/user/details \
  -H "Content-Type: application/json" \
  -d '{"verificationToken":"<token>","companyName":"KaratKart Diamonds Pvt Ltd","gstNumber":"27AABCU9603R1ZM","email":"ops@karatkart.com","city":"Mumbai","country":"India","website":"https://karatkart.com"}'

# 6) Create PIN
curl -s -X POST http://localhost:8081/api/v1/auth/pin/create \
  -H "Content-Type: application/json" \
  -d '{"verificationToken":"<token>","pin":"258147","confirmPin":"258147"}'

# 7) Verify PIN (returning user)
curl -s -X POST http://localhost:8081/api/v1/auth/pin/verify \
  -H "Content-Type: application/json" \
  -d '{"countryCode":"+91","mobileNumber":"9876543210","pin":"258147"}'
```

## Web QR flow

```bash
# Web creates QR
curl -s -X POST http://localhost:8081/api/v1/auth/qr/create \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"web-browser-1"}'
# Render data.qrPayload as a QR image (deep link: karatkart://qr-login?sessionId=...)

# Web polls
curl -s http://localhost:8081/api/v1/auth/qr/<qrSessionId>

# Mobile confirms after PIN/OTP login (has accessToken)
curl -s -X POST http://localhost:8081/api/v1/auth/qr/<qrSessionId>/confirm \
  -H "Content-Type: application/json" \
  -d '{"accessToken":"<accessToken>"}'

# Web poll returns status=CONFIRMED + accessToken
```

## Config

| Property | Default | Notes |
|---|---|---|
| `marketplace.auth.otp.dev-expose-otp` | `true` | Returns OTP in JSON for local/dev |
| `marketplace.auth.otp.sms-enabled` | `false` | When true, calls notification-service SMS |
| `marketplace.notification.base-url` | `http://localhost:8089` | SMS provider host |
| `marketplace.auth.pin.length` | `6` | Matches KaratKart UI |
| `marketplace.security.enabled` | local=`false` | Auth endpoints also permitAll when security is on (token-gated in body) |

## Dependencies

- PostgreSQL database `identity_service` (Flyway `V1__mobile_auth.sql`)
- Redis (OTP, verification/access tokens, QR sessions)
- Optional: notification-service for SMS
