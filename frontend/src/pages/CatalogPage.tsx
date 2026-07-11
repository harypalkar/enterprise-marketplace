import { useState } from 'react';
import {
  createCategory,
  createProduct,
  createSeller,
  listCategories,
  listInventory,
  listPricing,
  listProducts,
} from '../api/services';
import { ResponsePanel } from '../components/ResponsePanel';
import { useAppContext } from '../context/AppContext';

export function CatalogPage() {
  const { token, flow, setFlow } = useAppContext();
  const [response, setResponse] = useState<unknown>(null);
  const [error, setError] = useState<string>();
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
          <h1>Catalog Services</h1>
          <p>Seller, Category, Product, Inventory, and Pricing APIs.</p>
        </div>
      </header>

      <section className="panel action-panel">
        <h2>Quick Actions</h2>
        <div className="action-grid">
          <button type="button" onClick={() => void run(async () => {
            const res = await createSeller(token || undefined, suffix);
            setFlow({ sellerId: res.data.id });
            return res;
          })}>Create Seller</button>
          <button type="button" onClick={() => void run(async () => {
            const res = await createCategory(token || undefined, suffix);
            setFlow({ categoryId: res.data.id });
            return res;
          })}>Create Category</button>
          <button type="button" onClick={() => void run(async () => {
            if (!flow.sellerId) throw new Error('Create a seller first');
            const res = await createProduct(token || undefined, flow.sellerId, suffix);
            setFlow({ productId: res.data.id });
            return res;
          })}>Create Product</button>
          <button type="button" onClick={() => void run(() => listProducts(token || undefined))}>List Products</button>
          <button type="button" onClick={() => void run(() => listCategories(token || undefined))}>List Categories</button>
          <button type="button" onClick={() => void run(() => listInventory(token || undefined))}>List Inventory</button>
          <button type="button" onClick={() => void run(() => listPricing(token || undefined))}>List Pricing</button>
        </div>
      </section>

      <ResponsePanel title="Last Response" data={response} error={error} />
    </div>
  );
}
