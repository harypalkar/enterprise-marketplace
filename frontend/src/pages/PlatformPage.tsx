import { useState } from 'react';
import {
  adminDashboard,
  listAudits,
  listNotifications,
  listReports,
  listSubscriptions,
  listWorkflows,
} from '../api/services';
import { ResponsePanel } from '../components/ResponsePanel';
import { useAppContext } from '../context/AppContext';

export function PlatformPage() {
  const { token } = useAppContext();
  const [response, setResponse] = useState<unknown>(null);
  const [error, setError] = useState<string>();

  async function run(action: () => Promise<unknown>) {
    setError(undefined);
    try {
      setResponse(await action());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed');
    }
  }

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Platform Services</h1>
          <p>Workflow, Notification, Audit, Subscription, Report, and Admin APIs.</p>
        </div>
      </header>

      <section className="panel action-panel">
        <div className="action-grid">
          <button type="button" onClick={() => void run(() => listWorkflows(token || undefined))}>Workflows</button>
          <button type="button" onClick={() => void run(() => listNotifications(token || undefined))}>Notifications</button>
          <button type="button" onClick={() => void run(() => listAudits(token || undefined))}>Audits</button>
          <button type="button" onClick={() => void run(() => listSubscriptions(token || undefined))}>Subscriptions</button>
          <button type="button" onClick={() => void run(() => listReports(token || undefined))}>Reports</button>
          <button type="button" onClick={() => void run(() => adminDashboard(token || undefined))}>Admin Dashboard</button>
        </div>
      </section>

      <ResponsePanel title="Last Response" data={response} error={error} />
    </div>
  );
}
