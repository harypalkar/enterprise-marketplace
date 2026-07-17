import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { useAppContext } from '../context/AppContext';

export function KaratShell() {
  const { flow } = useAppContext();
  const location = useLocation();
  const dark = location.pathname === '/' || location.pathname.startsWith('/auth');

  return (
    <div className="kk-app">
      <header className={`kk-header ${dark ? 'kk-header--dark' : ''}`}>
        <NavLink to="/" className="kk-brand">
          <span className="kk-brand__mark">K</span>
          <span>KaratKart</span>
        </NavLink>
        <nav className="kk-nav">
          <NavLink to="/marketplace" className={({ isActive }) => (isActive ? 'active kk-nav-hide-sm' : 'kk-nav-hide-sm')}>
            Marketplace
          </NavLink>
          <NavLink to="/auth/qr" className={({ isActive }) => (isActive ? 'active kk-nav-hide-sm' : 'kk-nav-hide-sm')}>
            QR Login
          </NavLink>
          <NavLink to="/console" className="kk-nav-hide-sm">
            API Console
          </NavLink>
          {flow.accessToken ? (
            <NavLink to="/marketplace" className="kk-btn kk-btn--gold" style={{ padding: '0.65rem 1.1rem' }}>
              Vault
            </NavLink>
          ) : (
            <NavLink to="/auth/sign-in" className="kk-btn kk-btn--gold" style={{ padding: '0.65rem 1.1rem' }}>
              Sign In
            </NavLink>
          )}
        </nav>
      </header>
      <main className="kk-main">
        <Outlet />
      </main>
      <footer className="kk-footer">
        <span>© {new Date().getFullYear()} KaratKart Luxury Exchange</span>
        <span>Secure diamond trading for collectors & professionals</span>
      </footer>
    </div>
  );
}
