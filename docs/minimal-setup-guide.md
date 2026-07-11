# Minimal 3-terminal setup for frontend testing

## Terminal 1 — Docker + Gateway

```powershell
cd docker
docker compose up -d

cd ..\gateway-service
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.11"
$env:MARKETPLACE_SECURITY_ENABLED = "false"
mvn spring-boot:run
```

**Verify:** http://localhost:8080/actuator/health

---

## Terminal 2 — Microservices (one command)

```powershell
cd scripts
.\start-minimal-services.ps1
```

This opens 14 service windows automatically. Wait ~60 seconds.

**Or start only what you need:**

```powershell
.\start-service.ps1 product-service
.\start-service.ps1 seller-service
.\start-service.ps1 search-service
```

---

## Terminal 3 — Frontend

```powershell
cd frontend
npm install
npm run dev
```

**Open:** http://localhost:5173

---

## Frontend URLs (test as user)

| Step | URL | What to do |
|------|-----|------------|
| 1 | http://localhost:5173/setup | Read setup guide |
| 2 | http://localhost:5173/ | Dashboard → Refresh |
| 3 | http://localhost:5173/services | Test **each service** one by one |
| 4 | http://localhost:5173/e2e | Run All Remaining |
| 5 | http://localhost:5173/catalog | Seller → Category → Product |
| 6 | http://localhost:5173/marketplace | Search + AI |
| 7 | http://localhost:5173/platform | Workflow, Notification, Admin |

---

## Minimum services for E2E wizard only

If your PC is slow, start only these 9:

1. gateway-service (Terminal 1)
2. product-service
3. seller-service
4. buyer-service
5. category-service
6. search-service
7. ai-service
8. notification-service
9. workflow-service
10. audit-service

```powershell
.\start-service.ps1 product-service
# ... repeat for each
```
