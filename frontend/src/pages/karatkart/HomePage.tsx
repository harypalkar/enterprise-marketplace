import { Link } from 'react-router-dom';

export function HomePage() {
  return (
    <>
      <section className="kk-hero">
        <div className="kk-hero__media" aria-hidden />
        <div className="kk-hero__veil" aria-hidden />
        <div className="kk-hero__content">
          <p className="kk-hero__brand">KaratKart</p>
          <h1 className="kk-hero__title">Discover exceptional diamonds</h1>
          <p className="kk-hero__lead">
            Join the global elite network of diamond traders. Certified stones, secure vault access, and a marketplace
            built for collectors and professionals.
          </p>
          <div className="kk-hero__actions">
            <Link to="/marketplace" className="kk-btn kk-btn--gold">
              Explore Marketplace
            </Link>
            <Link to="/auth/sign-in" className="kk-btn kk-btn--ghost">
              Secure Sign In
            </Link>
          </div>
        </div>
      </section>

      <section className="kk-section kk-section--band">
        <div className="kk-section__head">
          <h2>The collection</h2>
          <p>Explore the world of certified diamonds curated for collectors and wholesalers.</p>
        </div>
        <div className="kk-hero__actions" style={{ justifyContent: 'center' }}>
          <Link to="/marketplace" className="kk-btn kk-btn--gold">
            Browse diamonds
          </Link>
          <Link to="/auth/qr" className="kk-btn kk-btn--ink">
            Desktop QR login
          </Link>
        </div>
      </section>

      <section className="kk-section">
        <div className="kk-section__head">
          <h2>Latest insights & market trends</h2>
          <p>Expert analysis for the KaratKart elite exchange.</p>
        </div>
        <div className="kk-insights">
          <article className="kk-insight">
            <img
              src="https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?auto=format&fit=crop&w=900&q=80"
              alt="Diamond market insight"
            />
            <div className="kk-insight__body">
              <h3>Colorless demand rises</h3>
              <p>D–F grades continue to lead institutional buying this quarter.</p>
            </div>
          </article>
          <article className="kk-insight">
            <img
              src="https://images.unsplash.com/photo-1605100804763-247f67b3557e?auto=format&fit=crop&w=900&q=80"
              alt="Certified diamonds"
            />
            <div className="kk-insight__body">
              <h3>Certification clarity</h3>
              <p>Every listing is backed by GIA, IGI, or HRD digital certificates.</p>
            </div>
          </article>
          <article className="kk-insight">
            <img
              src="https://images.unsplash.com/photo-1617038260897-da713e940c4a?auto=format&fit=crop&w=900&q=80"
              alt="Diamond collection"
            />
            <div className="kk-insight__body">
              <h3>The collection</h3>
              <p>Explore rare cuts curated for private vaults and retail showrooms.</p>
            </div>
          </article>
        </div>
      </section>
    </>
  );
}
