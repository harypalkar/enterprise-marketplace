interface ResponsePanelProps {
  title: string;
  data: unknown;
  error?: string;
}

export function ResponsePanel({ title, data, error }: ResponsePanelProps) {
  return (
    <section className="panel response-panel">
      <div className="panel-header">
        <h3>{title}</h3>
        {error ? <span className="badge down">Error</span> : <span className="badge up">OK</span>}
      </div>
      {error ? <pre className="error-text">{error}</pre> : null}
      <pre>{data ? JSON.stringify(data, null, 2) : 'No response yet'}</pre>
    </section>
  );
}
