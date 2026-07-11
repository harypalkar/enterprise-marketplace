# Notification Provider Integration Guide

## Email

### SMTP (default)

```yaml
marketplace:
  notification:
    email-provider: SMTP
spring:
  mail:
    host: ${SMTP_HOST}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
```

### AWS SES

```yaml
marketplace:
  notification:
    email-provider: SES
    aws-region: ap-south-1
```

Requires AWS credentials via standard SDK provider chain (IAM role, env vars, or profile).

HTML templates use `content_type = HTML` in `notification_template`.

## SMS

### Twilio

```yaml
marketplace:
  notification:
    twilio-enabled: true
    twilio-account-sid: ${TWILIO_ACCOUNT_SID}
    twilio-auth-token: ${TWILIO_AUTH_TOKEN}
    twilio-from-number: ${TWILIO_FROM_NUMBER}
```

### HTTP Gateway (fallback)

```yaml
marketplace:
  notification:
    sms-gateway-url: https://sms-gateway.example.com/send
```

## Push (Firebase Cloud Messaging)

```yaml
marketplace:
  notification:
    fcm-enabled: true
    fcm-service-account-path: /secrets/firebase-service-account.json
```

Push templates use `content_type = PUSH` with JSON body templates.

### HTTP Gateway (fallback)

```yaml
marketplace:
  notification:
    push-gateway-url: https://push-gateway.example.com/notify
```

## Webhook

Set `recipientAddress` to the callback URL. Payload includes `notificationId`, `requestId`, `correlationId`, `recipientId`, `subject`, `body`, `notificationType`, and `channel`.

Webhook templates use `content_type = WEBHOOK`.

## In-App

No external provider. Notifications are persisted to `notification_inbox` and exposed via `/api/v1/inbox/recipient/{recipientId}`.

## Rate Limits

Channel rate limits are stored in `notification_channel.rate_limit_per_hour` and enforced via Redis counters per recipient/channel.

## User Preferences

User notification preferences are cached in Redis under `notification:preference:{userId}` as JSON (channels enabled, quiet hours, etc.).
