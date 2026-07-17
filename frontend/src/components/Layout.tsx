import { NavLink, Outlet } from 'react-router-dom';
import { useAppContext } from '../context/AppContext';

const NAV = [
  { to: '/console', label: 'Dashboard' },
  { to: '/console/setup', label: 'Setup Guide' },
  { to: '/console/services', label: 'All Services' },
  { to: '/console/e2e', label: 'E2E Flow' },
  { to: '/console/catalog', label: 'Catalog' },
  { to: '/console/marketplace', label: 'Marketplace APIs' },
  { to: '/console/platform', label: 'Platform' },
  { to: '/', label: '← KaratKart Site' },
];

export function Layout() {
  const { token, setToken } = useAppContext();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">EM</span>
          <div>
            <strong>Enterprise Marketplace</strong>
            <p>End-to-end test console</p>
          </div>
        </div>
        <nav>
          {NAV.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === '/console' || item.to === '/'}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="token-box">
          <label htmlFor="token">JWT Bearer Token</label>
          <textarea
            id="token"
            rows={4}
            value={token}
            onChange={(e) => setToken(e.target.value)}
            placeholder="Paste Keycloak access token (optional if security disabled)"
          />
          <small>Gateway + services accept requests without token when `marketplace.security.enabled=false`.</small>
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
