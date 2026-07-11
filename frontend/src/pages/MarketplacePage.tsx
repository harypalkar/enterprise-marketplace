import { useState } from 'react';
import {
  aiChat,
  aiInterpretSearch,
  createBuyer,
  listBuyers,
  searchProductsElasticsearch,
} from '../api/services';
import { ResponsePanel } from '../components/ResponsePanel';
import { useAppContext } from '../context/AppContext';

export function MarketplacePage() {
  const { token, flow, setFlow } = useAppContext();
  const [response, setResponse] = useState<unknown>(null);
  const [error, setError] = useState<string>();
  const [searchQuery, setSearchQuery] = useState('steel');
  const [chatMessage, setChatMessage] = useState('Find industrial steel suppliers');
  const [suffix] = useState(() => Date.now().toString().slice(-6));

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
          <h1>Marketplace Experience</h1>
          <p>Buyer flows, Elasticsearch search, and AI assistant.</p>
        </div>
      </header>

      <section className="panel action-panel">
        <h2>Buyer</h2>
        <div className="action-grid">
          <button type="button" onClick={() => void run(async () => {
            const res = await createBuyer(token || undefined, suffix);
            setFlow({ buyerId: res.data.id });
            return res;
          })}>Create Buyer</button>
          <button type="button" onClick={() => void run(() => listBuyers(token || undefined))}>List Buyers</button>
        </div>
      </section>

      <section className="panel action-panel">
        <h2>Search</h2>
        <div className="form-row">
          <input value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} placeholder="Search query" />
          <button type="button" onClick={() => void run(() => searchProductsElasticsearch(token || undefined, searchQuery))}>
            Search Products (Elasticsearch)
          </button>
          <button type="button" onClick={() => void run(() => aiInterpretSearch(token || undefined, searchQuery))}>
            AI Interpret Query
          </button>
        </div>
      </section>

      <section className="panel action-panel">
        <h2>AI Assistant</h2>
        <div className="form-row">
          <input value={chatMessage} onChange={(e) => setChatMessage(e.target.value)} placeholder="Chat message" />
          <button
            type="button"
            onClick={() =>
              void run(() => aiChat(token || undefined, chatMessage, flow.buyerId ?? 'buyer-ui'))
            }
          >
            Send Chat
          </button>
        </div>
      </section>

      <ResponsePanel title="Last Response" data={response} error={error} />
    </div>
  );
}
