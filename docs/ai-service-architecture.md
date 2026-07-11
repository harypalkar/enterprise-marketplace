# AI Service Architecture

## Purpose

The AI Service provides LLM-powered capabilities for the Enterprise Marketplace Platform using self-hosted Ollama. It supports buyer chat, seller product description generation, natural-language search interpretation, and recommendations.

## Components

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────┐
│  REST API       │────▶│  AiServiceImpl   │────▶│ OllamaProvider│
│  /api/v1/ai/*   │     │  (business logic)│     │  WebClient   │
└─────────────────┘     └────────┬─────────┘     └─────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    ▼              ▼              ▼
              PostgreSQL       Redis          Outbox → Kafka
           (sessions, logs)   (cache/flags)   (ai-* events)
```

## Data Model

| Table | Purpose |
|-------|---------|
| `ai_chat_session` | Chat session metadata per user |
| `ai_chat_message` | User/assistant message history |
| `ai_prompt_template` | Versioned prompts per use case |
| `ai_generation_log` | Request/response audit trail |
| `ai_audit` | Operation-level audit |
| `outbox_event` | Reliable Kafka publishing |

## Event Integration

1. **product-created** — Cache product snapshot; auto-generate description if blank
2. **search-index** — Cache latest product data for recommendations
3. **admin-feature-toggled** — Toggle AI feature flag in Redis (`feature=ai`)

## Security

JWT via Keycloak. Role matrix:

- **BUYER/SELLER/ADMIN** — chat, search interpret, recommendations
- **SELLER/ADMIN** — description generation

## Resilience

- Outbox pattern for Kafka publish with retry/dead-letter
- Ollama timeout configurable (`marketplace.ollama.timeout-seconds`)
- Feature flag allows admin disable without redeploy
- Standalone profile disables DB/Kafka/Redis/security for local smoke tests

## Observability

- Actuator health (readiness/liveness)
- `/api/v1/infrastructure/health/ollama` — Ollama connectivity
- Prometheus metrics, OpenTelemetry tracing
- Structured logging with correlation/request IDs
