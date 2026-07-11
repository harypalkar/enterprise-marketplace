# Marketplace UI

React test console for end-to-end verification of all Enterprise Marketplace microservices through the API Gateway.

## Quick Start

### 1. Start backend infrastructure and services

```powershell
cd docker
docker compose up -d

# Gateway (disable JWT for local UI testing)
cd ../gateway-service
$env:MARKETPLACE_SECURITY_ENABLED="false"
mvn spring-boot:run

# Core services (separate terminals, standalone profile recommended)
cd ../product-service
mvn spring-boot:run "-Dspring-boot.run.profiles=local,standalone"
```

Repeat for seller, buyer, category, search, ai, notification, workflow, audit, subscription, report, and admin services.

### 2. Start frontend

```powershell
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**

The Vite dev server proxies `/api/*` to `http://localhost:8080` (gateway).

## Pages

| Page | URL | Purpose |
|------|-----|---------|
| **Setup Guide** | `/setup` | 3-terminal minimal startup instructions |
| **Dashboard** | `/` | Health check all microservices via gateway |
| **All Services** | `/services` | Individual test button for every microservice API |
| **E2E Flow** | `/e2e` | Step-by-step wizard: seller → category → buyer → product → search → AI → async verification |
| **Catalog** | `/catalog` | Seller, category, product, inventory, pricing APIs |
| **Marketplace** | `/marketplace` | Buyer registration, Elasticsearch search, AI chat |
| **Platform** | `/platform` | Workflow, notification, audit, subscription, report, admin |

## Authentication

- Paste a Keycloak JWT in the sidebar when security is enabled
- For local testing, run gateway and services with `marketplace.security.enabled=false`

## Build

```powershell
npm run build
npm run preview
```

## Documentation

- [Minimal Setup Guide](../docs/minimal-setup-guide.md)
- [End-to-End Testing Guide](../docs/end-to-end-testing-guide.md)
