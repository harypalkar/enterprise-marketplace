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

export async function listProducts(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(product, product.apiPath)}?page=0&size=10`, { token });
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

export async function listReports(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(report, report.apiPath)}?page=0&size=10`, { token });
}

export async function adminDashboard(token: Token) {
  return apiRequest<unknown>(`${gatewayUrl(admin, admin.apiPath)}/dashboard`, { token });
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
