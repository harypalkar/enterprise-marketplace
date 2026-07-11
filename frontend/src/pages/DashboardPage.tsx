import { useEffect, useState } from 'react';
import { checkServiceHealth } from '../api/services';
import { SERVICES } from '../types/api';

interface HealthRow {
  id: string;
  name: string;
  port: number;
  status: string;
  latencyMs: number;
  error?: string;
}

export function DashboardPage() {
  const [rows, setRows] = useState<HealthRow[]>([]);
  const [loading, setLoading] = useState(false);

  async function refresh() {
    setLoading(true);
    const results = await Promise.all(
      SERVICES.filter((s) => s.id !== 'gateway').map(async (service) => {
        const health = await checkServiceHealth(service.id);
        return {
          id: service.id,
          name: service.name,
          port: service.port,
          status: health.status,
          latencyMs: health.latencyMs,
          error: health.error,
        };
      }),
    );
    setRows(results);
    setLoading(false);
  }

  useEffect(() => {
    void refresh();
  }, []);

  const upCount = rows.filter((r) => r.status === 'UP').length;

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Service Dashboard</h1>
          <p>Health checks through the API Gateway proxy at port 8080.</p>
        </div>
        <button type="button" onClick={() => void refresh()} disabled={loading}>
          {loading ? 'Checking…' : 'Refresh'}
        </button>
      </header>

      <div className="stats-grid">
        <article className="stat-card">
          <span>Services UP</span>
          <strong>
            {upCount}/{rows.length}
          </strong>
        </article>
        <article className="stat-card">
          <span>Gateway</span>
          <strong>http://localhost:8080</strong>
        </article>
        <article className="stat-card">
          <span>UI Dev Server</span>
          <strong>http://localhost:5173</strong>
        </article>
      </div>

      <section className="panel">
        <div className="panel-header">
          <h2>Microservice Health</h2>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Service</th>
                <th>Port</th>
                <th>Status</th>
                <th>Latency</th>
                <th>Note</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.name}</td>
                  <td>{row.port}</td>
                  <td>
                    <span className={`badge ${row.status === 'UP' ? 'up' : 'down'}`}>{row.status}</span>
                  </td>
                  <td>{row.latencyMs} ms</td>
                  <td>{row.error ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
