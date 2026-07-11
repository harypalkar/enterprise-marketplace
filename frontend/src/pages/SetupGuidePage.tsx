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
        <h2>Terminal 1 — Infrastructure + Gateway</h2>
        <pre>{`cd docker
docker compose up -d

cd ..\\gateway-service
$env:JAVA_HOME = "C:\\Program Files\\Java\\jdk-21.0.11"
$env:MARKETPLACE_SECURITY_ENABLED = "false"
mvn spring-boot:run`}</pre>
        <p><strong>Check:</strong> <a href="http://localhost:8080/actuator/health" target="_blank" rel="noreferrer">http://localhost:8080/actuator/health</a></p>
      </section>

      <section className="panel">
        <h2>Terminal 2 — Core microservices (batch script)</h2>
        <pre>{`cd scripts
.\\start-minimal-services.ps1`}</pre>
        <p>Starts: Product, Seller, Buyer, Category, Search, AI, Notification, Workflow, Audit (standalone profile).</p>
        <p>Or run manually one service:</p>
        <pre>{`.\\start-service.ps1 product-service`}</pre>
      </section>

      <section className="panel">
        <h2>Terminal 3 — Frontend</h2>
        <pre>{`cd frontend
npm install
npm run dev`}</pre>
        <p><strong>Open:</strong> <a href="http://localhost:5173" target="_blank" rel="noreferrer">http://localhost:5173</a></p>
      </section>

      <section className="panel">
        <h2>Test order (as a user)</h2>
        <ol className="setup-steps">
          <li><span>1</span><div><strong>Dashboard</strong><p><a href="http://localhost:5173/">http://localhost:5173/</a> — Refresh, confirm services UP</p></div></li>
          <li><span>2</span><div><strong>All Services</strong><p><a href="http://localhost:5173/services">http://localhost:5173/services</a> — Test each service one by one</p></div></li>
          <li><span>3</span><div><strong>E2E Flow</strong><p><a href="http://localhost:5173/e2e">http://localhost:5173/e2e</a> — Click Run All Remaining</p></div></li>
          <li><span>4</span><div><strong>Catalog</strong><p><a href="http://localhost:5173/catalog">http://localhost:5173/catalog</a> — Seller → Category → Product</p></div></li>
          <li><span>5</span><div><strong>Marketplace</strong><p><a href="http://localhost:5173/marketplace">http://localhost:5173/marketplace</a> — Buyer, Search, AI</p></div></li>
          <li><span>6</span><div><strong>Platform</strong><p><a href="http://localhost:5173/platform">http://localhost:5173/platform</a> — Workflow, Notification, Audit, Admin</p></div></li>
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
