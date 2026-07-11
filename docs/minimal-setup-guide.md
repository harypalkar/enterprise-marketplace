# Minimal 3-terminal setup for frontend testing

## Terminal 1 — Gateway (port 9080)

```powershell
cd scripts
.\start-gateway.ps1
```

**Verify:** http://localhost:9080/actuator/health

> Port 8080 on your PC may be used by another app. Marketplace gateway uses **9080**.

---

## Terminal 2 — Essential microservices

```powershell
cd scripts
.\start-essential-services.ps1
```

Wait 60–90 seconds, then verify:

```powershell
.\check-setup.ps1
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
