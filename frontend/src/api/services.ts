import { apiRequest } from './client';
import { gatewayUrl, newGuid, newIdempotencyKey, SERVICES } from '../types/api';

const product = SERVICES.find((s) => s.id === 'product')!;
const seller = SERVICES.find((s) => s.id === 'seller')!;
const buyer = SERVICES.find((s) => s.id === 'buyer')!;
const category = SERVICES.find((s) => s.id === 'category')!;
const inventory = SERVICES.find((s) => s.id === 'inventory')!;
const pricing = SERVICES.find((s) => s.id === 'pricing')!;
const workflow = SERVICES.find((s) => s.id === 'workflow')!;
const notification = SERVICES.find((s) => s.id === 'notification')!;
const search = SERVICES.find((s) => s.id === 'search')!;
const ai = SERVICES.find((s) => s.id === 'ai')!;
const audit = SERVICES.find((s) => s.id === 'audit')!;
const subscription = SERVICES.find((s) => s.id === 'subscription')!;
const report = SERVICES.find((s) => s.id === 'report')!;
const admin = SERVICES.find((s) => s.id === 'admin')!;
const identity = SERVICES.find((s) => s.id === 'identity')!;

type Token = string | undefined;

export async function checkServiceHealth(serviceId: string) {
  const service = SERVICES.find((s) => s.id === serviceId);
  if (!service) {
    throw new Error('Unknown service');
  }
  const url = gatewayUrl(service, service.bootstrapPath);
  const started = performance.now();
  try {
    await apiRequest<Record<string, string>>(url);
    return { status: 'UP', latencyMs: Math.round(performance.now() - started) };
  } catch (error) {
    return {
      status: 'DOWN',
      latencyMs: Math.round(performance.now() - started),
      error: error instanceof Error ? error.message : 'Unknown error',
    };
  }
}

export async function checkElasticsearchHealth() {
  return apiRequest<unknown>(gatewayUrl(search, '/api/v1/infrastructure/health/elasticsearch'));
}

export async function checkAiHealth() {
  return apiRequest<unknown>(gatewayUrl(ai, '/api/v1/infrastructure/health/ollama'));
}

export function buildProductPayload(sellerId: string, sku: string, name: string) {
  const idempotencyKey = newIdempotencyKey();
  return {
    header: { sourceSystem: 'marketplace-ui', channel: 'WEB', locale: 'en-IN' },
    requestInfo: {
      clientRequestId: newGuid(),
      idempotencyKey,
      requestedBy: 'marketplace-ui',
    },
    seller: { sellerId },
    product: {
      sku,
      name,
      description: 'Created from Enterprise Marketplace UI',
      unitOfMeasure: 'PCS',
      status: 'ACTIVE',
    },
    pricing: {
      unitPrice: 1500,
      currency: 'INR',
      minQuantity: 1,
      validFrom: new Date().toISOString(),
    },
    inventory: {
      quantityAvailable: 100,
      quantityReserved: 0,
      reorderLevel: 10,
      warehouseCode: 'WH-01',
    },
    attributes: [{ key: 'material', value: 'steel' }],
    media: [
      {
        url: 'https://example.com/product.jpg',
        altText: name,
        displayOrder: 1,
        primaryImage: true,
      },
    ],
  };
}

export async function createSeller(token: Token, suffix: string) {
  return apiRequest<{ id: string }>(gatewayUrl(seller, seller.apiPath), {
    method: 'POST',
    token,
    idempotencyKey: newIdempotencyKey(),
    body: {
      companyName: `UI Seller ${suffix}`,
      tradeName: `Trade ${suffix}`,
      gstin: '27AABCU9603R1ZM',
      pan: 'AABCU9603R',
      email: `seller.${suffix}@marketplace.test`,
      phone: '9876543210',
      city: 'Mumbai',
      state: 'Maharashtra',
      country: 'India',
      pinCode: '400001',
      status: 'ACTIVE',
    },
  });
}

export async function createBuyer(token: Token, suffix: string) {
  return apiRequest<{ id: string }>(gatewayUrl(buyer, buyer.apiPath), {
    method: 'POST',
    token,
    idempotencyKey: newIdempotencyKey(),
    body: {
      companyName: `UI Buyer ${suffix}`,
      contactPerson: 'Test Buyer',
      email: `buyer.${suffix}@marketplace.test`,
      phone: '+91-9876543211',
      city: 'Pune',
      state: 'Maharashtra',
      country: 'India',
      pinCode: '411001',
      status: 'ACTIVE',
    },
  });
}

export async function createCategory(token: Token, suffix: string) {
  return apiRequest<{ id: string }>(gatewayUrl(category, category.apiPath), {
    method: 'POST',
    token,
    idempotencyKey: newIdempotencyKey(),
    body: {
      name: `Industrial ${suffix}`,
      slug: `industrial-${suffix.toLowerCase()}`,
      description: 'Category created from UI',
      status: 'ACTIVE',
    },
  });
}

export async function createProduct(token: Token, sellerId: string, suffix: string) {
  const body = buildProductPayload(sellerId, `UI-SKU-${suffix}`, `UI Product ${suffix}`);
  return apiRequest<{ id: string }>(gatewayUrl(product, product.apiPath), {
    method: 'POST',
    token,
    idempotencyKey: body.requestInfo.idempotencyKey,
    body,
  });
}

export async function getProductById(token: Token, productId: string) {
  return apiRequest<unknown>(`${gatewayUrl(product, product.apiPath)}/${productId}`, { token });
}

export async function listProducts(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(product, product.apiPath)}?page=0&size=10`, { token });
}

export async function createInventory(token: Token, productId: string, sellerId: string) {
  return apiRequest<unknown>(gatewayUrl(inventory, inventory.apiPath), {
    method: 'POST',
    token,
    idempotencyKey: newIdempotencyKey(),
    body: {
      productId,
      sellerId,
      quantityAvailable: 250,
      quantityReserved: 0,
      reorderLevel: 20,
      warehouseCode: 'WH-UI',
      status: 'ACTIVE',
    },
  });
}

export async function createPricing(token: Token, productId: string, sellerId: string) {
  return apiRequest<unknown>(gatewayUrl(pricing, pricing.apiPath), {
    method: 'POST',
    token,
    idempotencyKey: newIdempotencyKey(),
    body: {
      productId,
      sellerId,
      unitPrice: 1750,
      currency: 'INR',
      minQuantity: 1,
      discountPercent: 0,
      validFrom: new Date().toISOString(),
      status: 'ACTIVE',
    },
  });
}

export async function createWorkflow(token: Token, productId: string) {
  return apiRequest<unknown>(gatewayUrl(workflow, workflow.apiPath), {
    method: 'POST',
    token,
    idempotencyKey: newIdempotencyKey(),
    body: {
      requestId: newGuid(),
      correlationId: newGuid(),
      aggregateType: 'PRODUCT',
      aggregateId: productId,
      operationType: 'CREATE',
      sourceSystem: 'marketplace-ui',
      initiatedBy: 'ui-tester',
      message: 'Workflow created from UI',
      initialStatus: 'INITIATED',
    },
  });
}

export async function sendNotification(token: Token, recipientId: string) {
  return apiRequest<unknown>(gatewayUrl(notification, `${notification.apiPath}/send`), {
    method: 'POST',
    token,
    idempotencyKey: newIdempotencyKey(),
    body: {
      requestId: newGuid(),
      correlationId: newGuid(),
      notificationType: 'TRANSACTIONAL',
      channel: 'EMAIL',
      recipientId,
      recipientAddress: `${recipientId}@marketplace.test`,
      subject: 'UI Test Notification',
      body: 'This notification was sent from the Marketplace UI tester.',
    },
  });
}

export async function searchProductsElasticsearch(token: Token, query: string) {
  return apiRequest<unknown>(
    `${gatewayUrl(search, search.apiPath)}/products?q=${encodeURIComponent(query)}&page=0&size=10`,
    { token },
  );
}

export async function aiChat(token: Token, message: string, userId: string) {
  return apiRequest<unknown>(gatewayUrl(ai, `${ai.apiPath}/chat`), {
    method: 'POST',
    token,
    body: { message, userId, userRole: 'BUYER' },
  });
}

export async function aiInterpretSearch(token: Token, query: string) {
  return apiRequest<unknown>(gatewayUrl(ai, `${ai.apiPath}/search/interpret`), {
    method: 'POST',
    token,
    body: { query },
  });
}

export async function aiRecommendations(token: Token, buyerId?: string) {
  const q = buyerId ? `?buyerId=${buyerId}&limit=5` : '?limit=5';
  return apiRequest<unknown>(`${gatewayUrl(ai, ai.apiPath)}/recommendations${q}`, { token });
}

export async function aiGenerateDescription(token: Token, productId: string, name: string) {
  return apiRequest<unknown>(gatewayUrl(ai, `${ai.apiPath}/generate/description`), {
    method: 'POST',
    token,
    body: {
      productId,
      name,
      sku: `SKU-${productId.slice(0, 8)}`,
      attributes: { material: 'steel' },
    },
  });
}

export async function listNotifications(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(notification, notification.apiPath)}?page=0&size=10`, { token });
}

export async function listWorkflows(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(workflow, workflow.apiPath)}?page=0&size=10`, { token });
}

export async function listAudits(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(audit, audit.apiPath)}?page=0&size=10`, { token });
}

export async function listSubscriptions(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(subscription, subscription.apiPath)}?page=0&size=10`, { token });
}

export async function listPlans(token: Token) {
  return apiRequest<unknown>(gatewayUrl(subscription, '/api/v1/plans'), { token });
}

export async function createPlan(token: Token, suffix: string) {
  return apiRequest<unknown>(gatewayUrl(subscription, '/api/v1/plans'), {
    method: 'POST',
    token,
    idempotencyKey: newIdempotencyKey(),
    body: {
      planCode: `UI-${suffix}`,
      name: `UI Plan ${suffix}`,
      tier: 'STANDARD',
      price: 999,
      currency: 'INR',
      billingCycle: 'MONTHLY',
      features: { products: 100, users: 5 },
    },
  });
}

export async function listReports(token: Token) {
  return listReportJobs(token);
}

export async function listReportJobs(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(report, report.apiPath)}/jobs?page=0&size=10`, { token });
}

export async function listReportDefinitions(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(report, report.apiPath)}/definitions`, { token });
}

export async function adminDashboard(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(admin, admin.apiPath)}/dashboard`, { token });
}

export async function adminSettings(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(admin, admin.apiPath)}/settings`, { token });
}

export async function adminFeatureFlags(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(admin, admin.apiPath)}/feature-flags`, { token });
}

export async function listInventory(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(inventory, inventory.apiPath)}?page=0&size=10`, { token });
}

export async function listPricing(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(pricing, pricing.apiPath)}?page=0&size=10`, { token });
}

export async function listSellers(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(seller, seller.apiPath)}?page=0&size=10`, { token });
}

export async function listBuyers(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(buyer, buyer.apiPath)}?page=0&size=10`, { token });
}

export async function listCategories(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(category, category.apiPath)}?page=0&size=10`, { token });
}

export async function sendOtp(mobileNumber: string) {
  return apiRequest<{
    sessionId: string;
    expiresInSeconds: number;
    otp?: string;
    countryCode: string;
    mobileNumber: string;
  }>(gatewayUrl(identity, '/api/v1/auth/otp/send'), {
    method: 'POST',
    body: { countryCode: '+91', mobileNumber },
  });
}

export async function resendOtp(sessionId: string) {
  return apiRequest<{ sessionId: string; otp?: string }>(gatewayUrl(identity, '/api/v1/auth/otp/resend'), {
    method: 'POST',
    body: { sessionId },
  });
}

export async function verifyOtp(sessionId: string, otp: string) {
  return apiRequest<{
    verificationToken: string;
    isNewUser: boolean;
    userId?: string;
    mobileNumber: string;
  }>(gatewayUrl(identity, '/api/v1/auth/otp/verify'), {
    method: 'POST',
    body: { sessionId, otp },
  });
}

export async function setUserType(verificationToken: string, userType: 'INDIVIDUAL' | 'BUSINESS') {
  return apiRequest<{ userId: string; userType: string }>(gatewayUrl(identity, '/api/v1/auth/user/type'), {
    method: 'POST',
    body: { verificationToken, userType },
  });
}

export async function setUserDetails(
  verificationToken: string,
  userType: 'INDIVIDUAL' | 'BUSINESS',
  details: {
    fullName?: string;
    email: string;
    companyName?: string;
    website?: string;
    gstNumber?: string;
    city?: string;
    country?: string;
  },
) {
  return apiRequest<unknown>(gatewayUrl(identity, '/api/v1/auth/user/details'), {
    method: 'POST',
    body: {
      verificationToken,
      ...(userType === 'INDIVIDUAL'
        ? { fullName: details.fullName, email: details.email }
        : {
            companyName: details.companyName,
            gstNumber: details.gstNumber,
            email: details.email,
            city: details.city ?? 'Mumbai',
            country: details.country ?? 'India',
            website: details.website,
          }),
    },
  });
}

export async function setUserDetailsIndividual(verificationToken: string, suffix: string) {
  return setUserDetails(verificationToken, 'INDIVIDUAL', {
    fullName: `UI User ${suffix}`,
    email: `user.${suffix}@karatkart.test`,
  });
}

export async function setUserDetailsBusiness(verificationToken: string, suffix: string) {
  return setUserDetails(verificationToken, 'BUSINESS', {
    companyName: `UI Biz ${suffix}`,
    gstNumber: '27AABCU9603R1ZM',
    email: `biz.${suffix}@karatkart.test`,
    city: 'Mumbai',
    country: 'India',
    website: 'https://karatkart.test',
  });
}

export async function createPin(verificationToken: string, pin = '258147') {
  return apiRequest<{ accessToken: string; userId: string; userType: string }>(
    gatewayUrl(identity, '/api/v1/auth/pin/create'),
    {
      method: 'POST',
      body: { verificationToken, pin, confirmPin: pin },
    },
  );
}

export async function verifyPin(mobileNumber: string, pin = '258147') {
  return apiRequest<{ accessToken: string; userId: string; userType: string }>(
    gatewayUrl(identity, '/api/v1/auth/pin/verify'),
    {
      method: 'POST',
      body: { countryCode: '+91', mobileNumber, pin },
    },
  );
}

export async function createQrSession(deviceId = 'web-ui') {
  return apiRequest<{ qrSessionId: string; qrPayload: string; status: string; expiresInSeconds: number }>(
    gatewayUrl(identity, '/api/v1/auth/qr/create'),
    {
      method: 'POST',
      body: { deviceId },
    },
  );
}

export async function getQrSession(qrSessionId: string) {
  return apiRequest<{ qrSessionId: string; status: string; accessToken?: string }>(
    gatewayUrl(identity, `/api/v1/auth/qr/${qrSessionId}`),
  );
}

export async function confirmQrSession(qrSessionId: string, accessToken: string) {
  return apiRequest<{ qrSessionId: string; status: string; accessToken?: string }>(
    gatewayUrl(identity, `/api/v1/auth/qr/${qrSessionId}/confirm`),
    {
      method: 'POST',
      body: { accessToken },
    },
  );
}
