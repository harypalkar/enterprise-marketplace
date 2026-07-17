import { Link, useParams } from 'react-router-dom';
import { DIAMONDS, formatUsd } from '../../data/diamonds';

export function DiamondDetailPage() {
  const { id } = useParams();
  const diamond = DIAMONDS.find((d) => d.id === id) ?? DIAMONDS[0];
  const similar = DIAMONDS.filter((d) => d.id !== diamond.id).slice(0, 3);

  return (
    <>
      <section className="kk-detail">
        <div className="kk-detail__media">
          <img src={diamond.image} alt={diamond.title} />
          <span className="kk-detail__badge">Initialize 360° view</span>
        </div>
        <div className="kk-detail__info">
          <h1>{diamond.title} Diamond</h1>
          <p className="kk-price" style={{ fontSize: '1.4rem' }}>
            {formatUsd(diamond.price)}
          </p>
          <p className="kk-hint">
            {diamond.certified} Certified · {diamond.available ? 'Available for immediate delivery' : 'On request'}
          </p>
          <div className="kk-specs">
            <div>
              <span>Carat</span>
              <strong>{diamond.carat.toFixed(2)} CT</strong>
            </div>
            <div>
              <span>Color</span>
              <strong>{diamond.color}</strong>
            </div>
            <div>
              <span>Clarity</span>
              <strong>{diamond.clarity}</strong>
            </div>
            <div>
              <span>Cut</span>
              <strong>{diamond.cut}</strong>
            </div>
            <div>
              <span>Shape</span>
              <strong>{diamond.shape}</strong>
            </div>
            <div>
              <span>Polish</span>
              <strong>{diamond.polish}</strong>
            </div>
          </div>
          <Link to="/auth/sign-in" className="kk-btn kk-btn--gold">
            Request vault access
          </Link>
        </div>
      </section>
      <section className="kk-section">
        <div className="kk-section__head">
          <h2>Similar diamonds</h2>
          <p>Alternatives curated from the KaratKart collection.</p>
        </div>
        <div className="kk-grid">
          {similar.map((d) => (
            <article key={d.id} className="kk-diamond">
              <div className="kk-diamond__media">
                <img src={d.image} alt={d.title} />
              </div>
              <div className="kk-diamond__body">
                <h3>{d.title}</h3>
                <p className="kk-price">{formatUsd(d.price)}</p>
                <Link to={`/marketplace/${d.id}`} className="kk-btn kk-btn--ink" style={{ display: 'inline-block' }}>
                  View details
                </Link>
              </div>
            </article>
          ))}
        </div>
      </section>
    </>
  );
}
