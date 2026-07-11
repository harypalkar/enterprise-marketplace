import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { AppProvider } from './context/AppContext';
import { CatalogPage } from './pages/CatalogPage';
import { DashboardPage } from './pages/DashboardPage';
import { E2EFlowPage } from './pages/E2EFlowPage';
import { MarketplacePage } from './pages/MarketplacePage';
import { PlatformPage } from './pages/PlatformPage';
import { ServicesPage } from './pages/ServicesPage';
import { SetupGuidePage } from './pages/SetupGuidePage';

export default function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<DashboardPage />} />
            <Route path="setup" element={<SetupGuidePage />} />
            <Route path="services" element={<ServicesPage />} />
            <Route path="e2e" element={<E2EFlowPage />} />
            <Route path="catalog" element={<CatalogPage />} />
            <Route path="marketplace" element={<MarketplacePage />} />
            <Route path="platform" element={<PlatformPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AppProvider>
  );
}
