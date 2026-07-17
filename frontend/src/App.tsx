import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { KaratShell } from './components/KaratShell';
import { Layout } from './components/Layout';
import { AppProvider } from './context/AppContext';
import { CatalogPage } from './pages/CatalogPage';
import { DashboardPage } from './pages/DashboardPage';
import { E2EFlowPage } from './pages/E2EFlowPage';
import { CreatePinPage } from './pages/karatkart/CreatePinPage';
import { DetailsPage } from './pages/karatkart/DetailsPage';
import { DiamondDetailPage } from './pages/karatkart/DiamondDetailPage';
import { EnterPinPage } from './pages/karatkart/EnterPinPage';
import { HomePage } from './pages/karatkart/HomePage';
import { MarketPage } from './pages/karatkart/MarketPage';
import { OtpPage } from './pages/karatkart/OtpPage';
import { ProfileTypePage } from './pages/karatkart/ProfileTypePage';
import { QrLoginPage } from './pages/karatkart/QrLoginPage';
import { SignInPage } from './pages/karatkart/SignInPage';
import { MarketplacePage } from './pages/MarketplacePage';
import { PlatformPage } from './pages/PlatformPage';
import { ServicesPage } from './pages/ServicesPage';
import { SetupGuidePage } from './pages/SetupGuidePage';

export default function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<KaratShell />}>
            <Route index element={<HomePage />} />
            <Route path="marketplace" element={<MarketPage />} />
            <Route path="marketplace/:id" element={<DiamondDetailPage />} />
            <Route path="auth/sign-in" element={<SignInPage />} />
            <Route path="auth/otp" element={<OtpPage />} />
            <Route path="auth/profile-type" element={<ProfileTypePage />} />
            <Route path="auth/details" element={<DetailsPage />} />
            <Route path="auth/create-pin" element={<CreatePinPage />} />
            <Route path="auth/enter-pin" element={<EnterPinPage />} />
            <Route path="auth/qr" element={<QrLoginPage />} />
          </Route>

          <Route path="console" element={<Layout />}>
            <Route index element={<DashboardPage />} />
            <Route path="setup" element={<SetupGuidePage />} />
            <Route path="services" element={<ServicesPage />} />
            <Route path="e2e" element={<E2EFlowPage />} />
            <Route path="catalog" element={<CatalogPage />} />
            <Route path="marketplace" element={<MarketplacePage />} />
            <Route path="platform" element={<PlatformPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AppProvider>
  );
}
