import { useState } from 'react';
import {
  aiChat,
  aiInterpretSearch,
  createBuyer,
  createCategory,
  createProduct,
  createSeller,
  listAudits,
  listNotifications,
  listWorkflows,
  searchProductsElasticsearch,
} from '../api/services';
import { ResponsePanel } from '../components/ResponsePanel';
import { useAppContext } from '../context/AppContext';

const STEPS = [
  'Create Seller',
  'Create Category',
  'Create Buyer',
  'Create Product',
  'Search (Elasticsearch)',
  'AI Interpret + Chat',
  'Verify Async Events',
];

export function E2EFlowPage() {
  const { token, flow, setFlow } = useAppContext();
  const [step, setStep] = useState(0);
  const [suffix] = useState(() => Date.now().toString().slice(-6));
  const [log, setLog] = useState<{ step: string; data?: unknown; error?: string }[]>([]);
  const [running, setRunning] = useState(false);

  async function runStep(index: number) {
    setRunning(true);
    const stepName = STEPS[index];
    try {
      let data: unknown;
      switch (index) {
        case 0: {
          const res = await createSeller(token || undefined, suffix);
          setFlow({ sellerId: res.data.id });
          data = res;
          break;
        }
        case 1: {
          const res = await createCategory(token || undefined, suffix);
          setFlow({ categoryId: res.data.id });
          data = res;
          break;
        }
        case 2: {
          const res = await createBuyer(token || undefined, suffix);
          setFlow({ buyerId: res.data.id });
          data = res;
          break;
        }
        case 3: {
          if (!flow.sellerId) {
            throw new Error('Seller ID missing. Run step 1 first.');
          }
          const res = await createProduct(token || undefined, flow.sellerId, suffix);
          setFlow({ productId: res.data.id });
          data = res;
          break;
        }
        case 4: {
          data = await searchProductsElasticsearch(token || undefined, `UI Product ${suffix}`);
          break;
        }
        case 5: {
          const interpret = await aiInterpretSearch(token || undefined, 'steel products under 2000 INR');
          const chat = await aiChat(
            token || undefined,
            'Suggest suppliers for industrial steel rods',
            flow.buyerId ?? 'buyer-ui',
          );
          data = { interpret, chat };
          break;
        }
        case 6: {
          const notifications = await listNotifications(token || undefined);
          const workflows = await listWorkflows(token || undefined);
          const audits = await listAudits(token || undefined);
          data = { notifications, workflows, audits };
          break;
        }
        default:
          data = null;
      }
      setLog((prev) => [...prev, { step: stepName, data }]);
      setStep(index + 1);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown error';
      setLog((prev) => [...prev, { step: stepName, error: message }]);
    } finally {
      setRunning(false);
    }
  }

  async function runAll() {
    for (let i = step; i < STEPS.length; i += 1) {
      await runStep(i);
    }
  }

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>End-to-End Flow Wizard</h1>
          <p>Walk through the full B2B marketplace journey via the API Gateway.</p>
        </div>
        <div className="actions">
          <button type="button" onClick={() => void runStep(step)} disabled={running || step >= STEPS.length}>
            Run Current Step
          </button>
          <button type="button" className="primary" onClick={() => void runAll()} disabled={running || step >= STEPS.length}>
            Run All Remaining
          </button>
        </div>
      </header>

      <section className="panel flow-panel">
        <h2>Flow Context</h2>
        <div className="context-grid">
          <div><span>Seller ID</span><code>{flow.sellerId ?? '—'}</code></div>
          <div><span>Category ID</span><code>{flow.categoryId ?? '—'}</code></div>
          <div><span>Buyer ID</span><code>{flow.buyerId ?? '—'}</code></div>
          <div><span>Product ID</span><code>{flow.productId ?? '—'}</code></div>
        </div>
      </section>

      <section className="panel">
        <h2>Steps</h2>
        <ol className="step-list">
          {STEPS.map((label, index) => (
            <li key={label} className={index < step ? 'done' : index === step ? 'active' : ''}>
              <span>{index + 1}</span>
              <div>
                <strong>{label}</strong>
                <p>
                  {index === 0 && 'Register seller company via Seller Service'}
                  {index === 1 && 'Create taxonomy category via Category Service'}
                  {index === 2 && 'Register buyer company via Buyer Service'}
                  {index === 3 && 'Create canonical product → Kafka → search/notification/workflow/audit/AI'}
                  {index === 4 && 'Query Elasticsearch index via Search Service (async indexing)'}
                  {index === 5 && 'Use AI Service for NL search interpret and buyer chat'}
                  {index === 6 && 'Verify notification, workflow, and audit side effects'}
                </p>
              </div>
            </li>
          ))}
        </ol>
      </section>

      <div className="grid-2">
        {log.map((entry, index) => (
          <ResponsePanel key={`${entry.step}-${index}`} title={entry.step} data={entry.data} error={entry.error} />
        ))}
      </div>
    </div>
  );
}
