import type { ReactNode } from 'react';

export interface ServiceAction {
  id: string;
  label: string;
  description: string;
  gatewayUrl: string;
  run: () => Promise<unknown>;
}

export interface ServiceTestGroup {
  serviceId: string;
  description: string;
  actions: ServiceAction[];
}

export function action(
  id: string,
  label: string,
  description: string,
  gatewayUrl: string,
  run: () => Promise<unknown>,
): ServiceAction {
  return { id, label, description, gatewayUrl, run };
}

export interface ServiceCardProps {
  serviceId: string;
  name: string;
  port: number;
  description: string;
  actions: ServiceAction[];
  onResult: (title: string, data: unknown, error?: string) => void;
}

export function ServiceCard({ name, port, description, actions, onResult }: ServiceCardProps) {
  async function execute(actionItem: ServiceAction) {
    try {
      const data = await actionItem.run();
      onResult(`${name} — ${actionItem.label}`, data);
    } catch (error) {
      onResult(
        `${name} — ${actionItem.label}`,
        null,
        error instanceof Error ? error.message : 'Request failed',
      );
    }
  }

  return (
    <article className="service-card">
      <header>
        <div>
          <h3>{name}</h3>
          <p>{description}</p>
        </div>
        <span className="port-badge">:{port}</span>
      </header>
      <div className="service-actions">
        {actions.map((item) => (
          <button key={item.id} type="button" onClick={() => void execute(item)} title={item.gatewayUrl}>
            <strong>{item.label}</strong>
            <span>{item.description}</span>
          </button>
        ))}
      </div>
    </article>
  );
}

export function ResultStack({
  items,
}: {
  items: { title: string; data: unknown; error?: string }[];
}) {
  if (items.length === 0) {
    return null;
  }
  return (
    <section className="panel">
      <h2>Responses</h2>
      <div className="grid-2">
        {items.map((item, index) => (
          <div key={`${item.title}-${index}`} className="response-panel">
            <div className="panel-header">
              <h3>{item.title}</h3>
              {item.error ? <span className="badge down">Error</span> : <span className="badge up">OK</span>}
            </div>
            {item.error ? <pre className="error-text">{item.error}</pre> : null}
            <pre>{item.data ? JSON.stringify(item.data, null, 2) : 'No data'}</pre>
          </div>
        ))}
      </div>
    </section>
  );
}

export function StepList({ children }: { children: ReactNode }) {
  return <ol className="setup-steps">{children}</ol>;
}

export function StepItem({ n, title, children }: { n: number; title: string; children: ReactNode }) {
  return (
    <li>
      <span>{n}</span>
      <div>
        <strong>{title}</strong>
        {children}
      </div>
    </li>
  );
}
