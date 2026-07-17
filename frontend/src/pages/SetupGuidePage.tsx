export function SetupGuidePage() {
  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Minimal 3-Terminal Setup</h1>
          <p>Fastest way to test all services from the frontend without starting 14 separate terminals.</p>
        </div>
      </header>

      <section className="panel">
        <h2>Terminal 1 — Gateway (port 9080)</h2>
        <pre>{`cd scripts
.\\start-gateway.ps1`}</pre>
        <p><strong>Check:</strong> <a href="http://localhost:9080/actuator/health" target="_blank" rel="noreferrer">http://localhost:9080/actuator/health</a></p>
        <p className="warn-text">If port 9080 is already in use, gateway is likely already running. Check http://localhost:9080/actuator/health — do NOT start twice. To restart: <code>.\stop-gateway.ps1</code> then <code>.\start-gateway.ps1</code></p>
      </section>

      <section className="panel">
        <h2>Terminal 2 — Core microservices (batch script)</h2>
        <pre>{`cd scripts
.\\start-essential-services.ps1`}</pre>
        <p>Starts 9 core services. Wait 60–90 seconds for Maven startup.</p>
        <p>Verify: <code>.\\check-setup.ps1</code></p>
      </section>

      <section className="panel">
        <h2>Terminal 3 — Frontend</h2>
        <pre>{`cd frontend
npm install
npm run dev

# OR from scripts folder:
cd scripts
.\\start-frontend.ps1`}</pre>
        <p><strong>Open:</strong> <a href="http://localhost:5173" target="_blank" rel="noreferrer">http://localhost:5173</a></p>
        <p className="warn-text">Use <code>cd frontend</code> from the enterprise-marketplace folder — NOT <code>cd ..\\frontend</code>.</p>
      </section>

      <section className="panel">
        <h2>Test order (as a user)</h2>
        <ol className="setup-steps">
          <li><span>1</span><div><strong>KaratKart site</strong><p><a href="http://localhost:5173/">http://localhost:5173/</a> — Luxury diamond storefront</p></div></li>
          <li><span>2</span><div><strong>API Console</strong><p><a href="http://localhost:5173/console">http://localhost:5173/console</a> — Dashboard health check</p></div></li>
          <li><span>3</span><div><strong>All Services</strong><p><a href="http://localhost:5173/console/services">http://localhost:5173/console/services</a> — Test each service</p></div></li>
          <li><span>4</span><div><strong>E2E Flow</strong><p><a href="http://localhost:5173/console/e2e">http://localhost:5173/console/e2e</a> — Click Run All Remaining</p></div></li>
          <li><span>5</span><div><strong>Catalog</strong><p><a href="http://localhost:5173/console/catalog">http://localhost:5173/console/catalog</a> — Seller → Category → Product</p></div></li>
          <li><span>6</span><div><strong>Platform</strong><p><a href="http://localhost:5173/console/platform">http://localhost:5173/console/platform</a> — Workflow, Notification, Audit</p></div></li>
        </ol>
      </section>

      <section className="panel">
        <h2>Optional services (Terminal 2 extras)</h2>
        <pre>{`.\\start-service.ps1 inventory-service
.\\start-service.ps1 pricing-service
.\\start-service.ps1 subscription-service
.\\start-service.ps1 report-service
.\\start-service.ps1 admin-service`}</pre>
      </section>
    </div>
  );
}
