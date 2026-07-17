import { useMemo, useState } from 'react';
import { useAppContext } from '../context/AppContext';
import {
  adminDashboard,
  adminFeatureFlags,
  adminSettings,
  aiChat,
  aiGenerateDescription,
  aiInterpretSearch,
  aiRecommendations,
  checkAiHealth,
  checkElasticsearchHealth,
  checkServiceHealth,
  confirmQrSession,
  createBuyer,
  createCategory,
  createInventory,
  createPin,
  createPlan,
  createPricing,
  createProduct,
  createQrSession,
  createSeller,
  createWorkflow,
  getProductById,
  getQrSession,
  listAudits,
  listBuyers,
  listCategories,
  listInventory,
  listNotifications,
  listPlans,
  listPricing,
  listProducts,
  listReportDefinitions,
  listReportJobs,
  listSellers,
  listSubscriptions,
  listWorkflows,
  resendOtp,
  searchProductsElasticsearch,
  sendNotification,
  sendOtp,
  setUserDetailsBusiness,
  setUserDetailsIndividual,
  setUserType,
  verifyOtp,
  verifyPin,
} from '../api/services';
import { gatewayUrl, SERVICES } from '../types/api';
import { action, ResultStack, ServiceCard, type ServiceAction } from '../components/ServiceTester';

export function ServicesPage() {
  const { token, flow, setFlow } = useAppContext();
  const [suffix] = useState(() => Date.now().toString().slice(-6));
  const [results, setResults] = useState<{ title: string; data: unknown; error?: string }[]>([]);
  const t = token || undefined;

  function pushResult(title: string, data: unknown, error?: string) {
    setResults((prev) => [{ title, data, error }, ...prev].slice(0, 12));
  }

  const groups = useMemo(() => {
    const s = (id: string) => SERVICES.find((x) => x.id === id)!;
    const gw = (id: string, path: string) => gatewayUrl(s(id), path);

    const health = (id: string): ServiceAction =>
      action('health', 'Health Check', 'Bootstrap health via gateway', gw(id, '/api/v1/bootstrap/health'), () =>
        checkServiceHealth(id),
      );

    return [
      {
        serviceId: 'identity',
        name: 'Identity / Mobile Auth',
        port: 8081,
        description: 'KaratKart OTP, user onboarding, PIN, and web QR login',
        actions: [
          health('identity'),
          action('otp-send', 'Send OTP', 'POST /auth/otp/send', gw('identity', '/api/v1/auth/otp/send'), async () => {
            const mobile = `9${suffix.padStart(9, '0')}`.slice(0, 10);
            const res = await sendOtp(mobile);
            setFlow({
              otpSessionId: res.data.sessionId,
              otpCode: res.data.otp,
              mobileNumber: res.data.mobileNumber,
            });
            return res;
          }),
          action('otp-resend', 'Resend OTP', 'POST /auth/otp/resend', gw('identity', '/api/v1/auth/otp/resend'), async () => {
            if (!flow.otpSessionId) throw new Error('Send OTP first');
            const res = await resendOtp(flow.otpSessionId);
            setFlow({ otpSessionId: res.data.sessionId, otpCode: res.data.otp });
            return res;
          }),
          action('otp-verify', 'Verify OTP', 'POST /auth/otp/verify', gw('identity', '/api/v1/auth/otp/verify'), async () => {
            if (!flow.otpSessionId || !flow.otpCode) throw new Error('Send OTP first (need sessionId + otp)');
            const res = await verifyOtp(flow.otpSessionId, flow.otpCode);
            setFlow({
              verificationToken: res.data.verificationToken,
              mobileUserId: res.data.userId,
            });
            return res;
          }),
          action('user-type', 'Set User Type', 'POST /auth/user/type (INDIVIDUAL)', gw('identity', '/api/v1/auth/user/type'), async () => {
            if (!flow.verificationToken) throw new Error('Verify OTP first');
            const res = await setUserType(flow.verificationToken, 'INDIVIDUAL');
            setFlow({ mobileUserId: res.data.userId });
            return res;
          }),
          action(
            'user-details',
            'Set User Details',
            'POST /auth/user/details (individual)',
            gw('identity', '/api/v1/auth/user/details'),
            async () => {
              if (!flow.verificationToken) throw new Error('Verify OTP first');
              return setUserDetailsIndividual(flow.verificationToken, suffix);
            },
          ),
          action(
            'user-details-biz',
            'Set Business Details',
            'POST /auth/user/details (business) — set type BUSINESS first in a fresh flow',
            gw('identity', '/api/v1/auth/user/details'),
            async () => {
              if (!flow.verificationToken) throw new Error('Verify OTP first');
              await setUserType(flow.verificationToken, 'BUSINESS');
              return setUserDetailsBusiness(flow.verificationToken, suffix);
            },
          ),
          action('pin-create', 'Create PIN', 'POST /auth/pin/create (258147)', gw('identity', '/api/v1/auth/pin/create'), async () => {
            if (!flow.verificationToken) throw new Error('Complete OTP + user details first');
            const res = await createPin(flow.verificationToken);
            setFlow({ accessToken: res.data.accessToken, mobileUserId: res.data.userId });
            return res;
          }),
          action('pin-verify', 'Verify PIN', 'POST /auth/pin/verify', gw('identity', '/api/v1/auth/pin/verify'), async () => {
            if (!flow.mobileNumber) throw new Error('Send OTP first to capture mobileNumber');
            const res = await verifyPin(flow.mobileNumber);
            setFlow({ accessToken: res.data.accessToken, mobileUserId: res.data.userId });
            return res;
          }),
          action('qr-create', 'Create QR', 'POST /auth/qr/create', gw('identity', '/api/v1/auth/qr/create'), async () => {
            const res = await createQrSession(`web-${suffix}`);
            setFlow({ qrSessionId: res.data.qrSessionId });
            return res;
          }),
          action('qr-poll', 'Poll QR', 'GET /auth/qr/{id}', gw('identity', '/api/v1/auth/qr/{id}'), async () => {
            if (!flow.qrSessionId) throw new Error('Create QR first');
            return getQrSession(flow.qrSessionId);
          }),
          action(
            'qr-confirm',
            'Confirm QR',
            'POST /auth/qr/{id}/confirm',
            gw('identity', '/api/v1/auth/qr/{id}/confirm'),
            async () => {
              if (!flow.qrSessionId || !flow.accessToken) throw new Error('Create QR and Create/Verify PIN first');
              return confirmQrSession(flow.qrSessionId, flow.accessToken);
            },
          ),
        ],
      },
      {
        serviceId: 'seller',
        name: 'Seller Service',
        port: 8083,
        description: 'Seller company registration and management',
        actions: [
          health('seller'),
          action('list', 'List Sellers', 'GET paginated sellers', `${gw('seller', '/api/v1/sellers')}?page=0&size=10`, () =>
            listSellers(t),
          ),
          action('create', 'Create Seller', 'POST new seller', gw('seller', '/api/v1/sellers'), async () => {
            const res = await createSeller(t, suffix);
            setFlow({ sellerId: res.data.id });
            return res;
          }),
        ],
      },
      {
        serviceId: 'buyer',
        name: 'Buyer Service',
        port: 8084,
        description: 'Buyer company registration',
        actions: [
          health('buyer'),
          action('list', 'List Buyers', 'GET paginated buyers', `${gw('buyer', '/api/v1/buyers')}?page=0&size=10`, () =>
            listBuyers(t),
          ),
          action('create', 'Create Buyer', 'POST new buyer', gw('buyer', '/api/v1/buyers'), async () => {
            const res = await createBuyer(t, suffix);
            setFlow({ buyerId: res.data.id });
            return res;
          }),
        ],
      },
      {
        serviceId: 'category',
        name: 'Category Service',
        port: 8085,
        description: 'Product taxonomy and categories',
        actions: [
          health('category'),
          action('list', 'List Categories', 'GET categories', `${gw('category', '/api/v1/categories')}?page=0&size=10`, () =>
            listCategories(t),
          ),
          action('create', 'Create Category', 'POST category', gw('category', '/api/v1/categories'), async () => {
            const res = await createCategory(t, suffix);
            setFlow({ categoryId: res.data.id });
            return res;
          }),
        ],
      },
      {
        serviceId: 'product',
        name: 'Product Service',
        port: 8082,
        description: 'Canonical product catalog with outbox events',
        actions: [
          health('product'),
          action('list', 'List Products', 'GET products', `${gw('product', '/api/v1/products')}?page=0&size=10`, () =>
            listProducts(t),
          ),
          action('create', 'Create Product', 'POST canonical product', gw('product', '/api/v1/products'), async () => {
            const sellerId = flow.sellerId;
            if (!sellerId) throw new Error('Create a seller first (Seller Service card)');
            const res = await createProduct(t, sellerId, suffix);
            setFlow({ productId: res.data.id });
            return res;
          }),
          action('get', 'Get Product by ID', 'GET product detail', gw('product', '/api/v1/products/{id}'), async () => {
            if (!flow.productId) throw new Error('Create a product first');
            return getProductById(t, flow.productId);
          }),
        ],
      },
      {
        serviceId: 'inventory',
        name: 'Inventory Service',
        port: 8086,
        description: 'Stock levels and warehouse inventory',
        actions: [
          health('inventory'),
          action('list', 'List Inventory', 'GET inventory records', `${gw('inventory', '/api/v1/inventory')}?page=0&size=10`, () =>
            listInventory(t),
          ),
          action('create', 'Create Inventory', 'POST inventory row', gw('inventory', '/api/v1/inventory'), async () => {
            if (!flow.productId || !flow.sellerId) throw new Error('Create seller and product first');
            return createInventory(t, flow.productId, flow.sellerId);
          }),
        ],
      },
      {
        serviceId: 'pricing',
        name: 'Pricing Service',
        port: 8087,
        description: 'Product pricing rules',
        actions: [
          health('pricing'),
          action('list', 'List Pricing', 'GET pricing records', `${gw('pricing', '/api/v1/pricing')}?page=0&size=10`, () =>
            listPricing(t),
          ),
          action('create', 'Create Pricing', 'POST pricing row', gw('pricing', '/api/v1/pricing'), async () => {
            if (!flow.productId || !flow.sellerId) throw new Error('Create seller and product first');
            return createPricing(t, flow.productId, flow.sellerId);
          }),
        ],
      },
      {
        serviceId: 'search',
        name: 'Search Service',
        port: 8090,
        description: 'Elasticsearch full-text product search',
        actions: [
          health('search'),
          action('infra', 'Elasticsearch Health', 'Infrastructure health', gw('search', '/api/v1/infrastructure/health/elasticsearch'), () =>
            checkElasticsearchHealth(),
          ),
          action('search', 'Search Products', 'GET full-text search', `${gw('search', '/api/v1/search/products')}?q=steel`, () =>
            searchProductsElasticsearch(t, 'steel'),
          ),
        ],
      },
      {
        serviceId: 'ai',
        name: 'AI Service',
        port: 8091,
        description: 'Ollama chat, descriptions, recommendations',
        actions: [
          health('ai'),
          action('ollama', 'Ollama Health', 'LLM connectivity', gw('ai', '/api/v1/infrastructure/health/ollama'), () =>
            checkAiHealth(),
          ),
          action('chat', 'AI Chat', 'POST buyer assistant', gw('ai', '/api/v1/ai/chat'), () =>
            aiChat(t, 'Find steel suppliers', flow.buyerId ?? 'buyer-ui'),
          ),
          action('interpret', 'Interpret Search', 'NL → filters', gw('ai', '/api/v1/ai/search/interpret'), () =>
            aiInterpretSearch(t, 'steel under 2000 INR'),
          ),
          action('recommend', 'Recommendations', 'GET AI suggestions', `${gw('ai', '/api/v1/ai/recommendations')}?limit=5`, () =>
            aiRecommendations(t, flow.buyerId),
          ),
          action('describe', 'Generate Description', 'POST product copy', gw('ai', '/api/v1/ai/generate/description'), async () => {
            if (!flow.productId) throw new Error('Create a product first');
            return aiGenerateDescription(t, flow.productId, `UI Product ${suffix}`);
          }),
        ],
      },
      {
        serviceId: 'workflow',
        name: 'Workflow Service',
        port: 8088,
        description: 'Business process lifecycle tracking',
        actions: [
          health('workflow'),
          action('list', 'List Workflows', 'GET workflows', `${gw('workflow', '/api/v1/workflows')}?page=0&size=10`, () =>
            listWorkflows(t),
          ),
          action('create', 'Create Workflow', 'POST workflow instance', gw('workflow', '/api/v1/workflows'), async () => {
            if (!flow.productId) throw new Error('Create a product first');
            return createWorkflow(t, flow.productId);
          }),
        ],
      },
      {
        serviceId: 'notification',
        name: 'Notification Service',
        port: 8089,
        description: 'Multi-channel notifications',
        actions: [
          health('notification'),
          action('list', 'List Notifications', 'GET notifications', `${gw('notification', '/api/v1/notifications')}?page=0&size=10`, () =>
            listNotifications(t),
          ),
          action('send', 'Send Notification', 'POST direct send', gw('notification', '/api/v1/notifications/send'), () =>
            sendNotification(t, flow.buyerId ?? 'buyer-ui'),
          ),
        ],
      },
      {
        serviceId: 'audit',
        name: 'Audit Service',
        port: 8092,
        description: 'Central audit trail',
        actions: [
          health('audit'),
          action('list', 'List Audits', 'GET audit records', `${gw('audit', '/api/v1/audits')}?page=0&size=10`, () => listAudits(t)),
          action('search', 'Search Audits', 'GET audit search', `${gw('audit', '/api/v1/audits/search')}?page=0&size=10`, () =>
            listAudits(t),
          ),
        ],
      },
      {
        serviceId: 'subscription',
        name: 'Subscription Service',
        port: 8093,
        description: 'Plans and billing subscriptions',
        actions: [
          health('subscription'),
          action('plans', 'List Plans', 'GET subscription plans', gw('subscription', '/api/v1/plans'), () => listPlans(t)),
          action('create-plan', 'Create Plan', 'POST plan', gw('subscription', '/api/v1/plans'), () => createPlan(t, suffix)),
          action('subs', 'List Subscriptions', 'GET subscriptions', `${gw('subscription', '/api/v1/subscriptions')}?page=0&size=10`, () =>
            listSubscriptions(t),
          ),
        ],
      },
      {
        serviceId: 'report',
        name: 'Report Service',
        port: 8094,
        description: 'Report generation jobs',
        actions: [
          health('report'),
          action('defs', 'Report Definitions', 'GET definitions', gw('report', '/api/v1/reports/definitions'), () =>
            listReportDefinitions(t),
          ),
          action('jobs', 'List Report Jobs', 'GET jobs', `${gw('report', '/api/v1/reports/jobs')}?page=0&size=10`, () =>
            listReportJobs(t),
          ),
        ],
      },
      {
        serviceId: 'admin',
        name: 'Admin Service',
        port: 8095,
        description: 'Platform settings and feature flags',
        actions: [
          health('admin'),
          action('dashboard', 'Admin Dashboard', 'GET dashboard', gw('admin', '/api/v1/admin/dashboard'), () =>
            adminDashboard(t),
          ),
          action('settings', 'List Settings', 'GET settings', gw('admin', '/api/v1/admin/settings'), () => adminSettings(t)),
          action('flags', 'Feature Flags', 'GET feature flags', gw('admin', '/api/v1/admin/feature-flags'), () =>
            adminFeatureFlags(t),
          ),
        ],
      },
    ];
  }, [t, suffix, flow, setFlow]);

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>All Services Tester</h1>
          <p>Click each button to test one API per microservice through the gateway at http://localhost:8080</p>
        </div>
        <button type="button" onClick={() => setResults([])}>Clear Responses</button>
      </header>

      <section className="panel flow-panel">
        <h2>Shared Context (from E2E / Catalog flows)</h2>
        <div className="context-grid">
          <div><span>Seller ID</span><code>{flow.sellerId ?? '—'}</code></div>
          <div><span>Buyer ID</span><code>{flow.buyerId ?? '—'}</code></div>
          <div><span>Category ID</span><code>{flow.categoryId ?? '—'}</code></div>
          <div><span>Product ID</span><code>{flow.productId ?? '—'}</code></div>
          <div><span>OTP Session</span><code>{flow.otpSessionId ?? '—'}</code></div>
          <div><span>Verification Token</span><code>{flow.verificationToken ?? '—'}</code></div>
          <div><span>Mobile Access Token</span><code>{flow.accessToken ?? '—'}</code></div>
          <div><span>QR Session</span><code>{flow.qrSessionId ?? '—'}</code></div>
        </div>
      </section>

      <div className="service-grid">
        {groups.map((group) => (
          <ServiceCard
            key={group.serviceId}
            serviceId={group.serviceId}
            name={group.name}
            port={group.port}
            description={group.description}
            actions={group.actions}
            onResult={pushResult}
          />
        ))}
      </div>

      <ResultStack items={results} />
    </div>
  );
}
