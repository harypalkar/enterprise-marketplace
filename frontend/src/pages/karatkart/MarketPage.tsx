import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { CLARITY_GRADES, DIAMOND_SHAPES, DIAMONDS, formatUsd } from '../../data/diamonds';

export function MarketPage() {
  const [shape, setShape] = useState<string>('All');
  const [clarity, setClarity] = useState<string>('All');
  const [maxCarat, setMaxCarat] = useState(3);
  const [maxPrice, setMaxPrice] = useState(20000);

  const items = useMemo(
    () =>
      DIAMONDS.filter((d) => (shape === 'All' ? true : d.shape === shape))
        .filter((d) => (clarity === 'All' ? true : d.clarity === clarity))
        .filter((d) => d.carat <= maxCarat && d.price <= maxPrice),
    [clarity, maxCarat, maxPrice, shape],
  );

  return (
    <section className="kk-section">
      <div className="kk-section__head">
        <h2>The collection</h2>
        <p>Explore certified diamonds with filters for shape, clarity, carat, and price.</p>
      </div>
      <div className="kk-market">
        <aside className="kk-filters">
          <h3>Filters</h3>
          <div className="kk-shapes">
            <button type="button" className={shape === 'All' ? 'active' : ''} onClick={() => setShape('All')}>
              All
            </button>
            {DIAMOND_SHAPES.map((s) => (
              <button key={s} type="button" className={shape === s ? 'active' : ''} onClick={() => setShape(s)}>
                {s}
              </button>
            ))}
          </div>
          <p style={{ fontSize: '0.75rem', letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--kk-muted)' }}>
            Clarity grade
          </p>
          <div className="kk-chips">
            <button type="button" className={clarity === 'All' ? 'active' : ''} onClick={() => setClarity('All')}>
              All
            </button>
            {CLARITY_GRADES.map((c) => (
              <button key={c} type="button" className={clarity === c ? 'active' : ''} onClick={() => setClarity(c)}>
                {c}
              </button>
            ))}
          </div>
          <div className="kk-range">
            <label>
              <span>Carat range</span>
              <span>to {maxCarat.toFixed(2)} ct</span>
            </label>
            <input type="range" min={0.5} max={3} step={0.05} value={maxCarat} onChange={(e) => setMaxCarat(Number(e.target.value))} />
          </div>
          <div className="kk-range">
            <label>
              <span>Price range</span>
              <span>to {formatUsd(maxPrice)}</span>
            </label>
            <input
              type="range"
              min={1000}
              max={20000}
              step={100}
              value={maxPrice}
              onChange={(e) => setMaxPrice(Number(e.target.value))}
            />
          </div>
        </aside>
        <div className="kk-grid">
          {items.map((d) => (
            <article key={d.id} className="kk-diamond">
              <div className="kk-diamond__media">
                <img src={d.image} alt={d.title} />
              </div>
              <div className="kk-diamond__body">
                <h3>{d.title}</h3>
                <p className="kk-diamond__meta">
                  {d.color} · {d.clarity} · {d.certified}
                </p>
                <p className="kk-price">{formatUsd(d.price)}</p>
                <Link to={`/marketplace/${d.id}`} className="kk-btn kk-btn--gold" style={{ display: 'inline-block' }}>
                  View details
                </Link>
              </div>
            </article>
          ))}
          {items.length === 0 ? <p className="kk-hint">No diamonds match these filters.</p> : null}
        </div>
      </div>
    </section>
  );
}
