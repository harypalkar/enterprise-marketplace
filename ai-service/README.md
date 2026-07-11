# AI Service

Production-ready Ollama-powered AI microservice for the Enterprise Marketplace Platform.

## End-to-End Flow

```
Product Service (create/update)
    → Kafka: product-created / search-index
    → AI Service consumer (cache + auto description)
    → Ollama: /api/generate
    → Outbox: ai-description-generated
    → Seller/Buyer: REST chat, search interpret, recommendations
```

## Overview

| Item | Value |
|------|-------|
| Port | 8091 |
| Base path | `/api/v1/ai` |
| Gateway | `http://localhost:8080/api/ai/api/v1/ai/chat` |
| LLM | Ollama (`llama3.2` default) |
| Metadata DB | Neon PostgreSQL (sessions, logs, prompts, audit, outbox) |
| Cache | Redis (prompt templates, product snapshots, feature flag) |
| Messaging | Kafka (consumes product/search/admin events, publishes AI events) |

## API Endpoints

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/api/v1/ai/chat` | Buyer/Seller/Admin | Conversational marketplace assistant |
| POST | `/api/v1/ai/generate/description` | Seller/Admin | Generate product description |
| POST | `/api/v1/ai/search/interpret` | Buyer/Seller/Admin | Natural language → search filters |
| GET | `/api/v1/ai/recommendations` | Buyer/Seller/Admin | AI product recommendations |

## Kafka Topics

**Consumed:** `product-created`, `product-updated`, `search-index`, `admin-feature-toggled`

**Published:** `ai-description-generated`, `ai-chat-completed`

## Run Locally

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
cd ai-service
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

With Ollama (Docker):

```powershell
cd docker
docker compose up -d ollama
```

## Build & Test

```powershell
mvn clean install -pl ai-service -am
```

## Documentation

- [AI Architecture](../docs/ai-service-architecture.md)
- [Postman Collection](../docs/postman/Enterprise-Marketplace-AI.postman_collection.json)
