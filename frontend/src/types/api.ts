export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  correlationId?: string;
  requestId?: string;
}

export interface ServiceDefinition {
  id: string;
  name: string;
  gatewayPrefix: string;
  apiPath: string;
  bootstrapPath: string;
  port: number;
}

export const SERVICES: ServiceDefinition[] = [
  { id: 'gateway', name: 'Gateway', gatewayPrefix: '', apiPath: '/api/v1/bootstrap', bootstrapPath: '/api/v1/bootstrap/health', port: 8080 },
  { id: 'product', name: 'Product', gatewayPrefix: '/api/products', apiPath: '/api/v1/products', bootstrapPath: '/api/v1/bootstrap/health', port: 8082 },
  { id: 'seller', name: 'Seller', gatewayPrefix: '/api/sellers', apiPath: '/api/v1/sellers', bootstrapPath: '/api/v1/bootstrap/health', port: 8083 },
  { id: 'buyer', name: 'Buyer', gatewayPrefix: '/api/buyers', apiPath: '/api/v1/buyers', bootstrapPath: '/api/v1/bootstrap/health', port: 8084 },
  { id: 'category', name: 'Category', gatewayPrefix: '/api/categories', apiPath: '/api/v1/categories', bootstrapPath: '/api/v1/bootstrap/health', port: 8085 },
  { id: 'inventory', name: 'Inventory', gatewayPrefix: '/api/inventory', apiPath: '/api/v1/inventory', bootstrapPath: '/api/v1/bootstrap/health', port: 8086 },
  { id: 'pricing', name: 'Pricing', gatewayPrefix: '/api/pricing', apiPath: '/api/v1/pricing', bootstrapPath: '/api/v1/bootstrap/health', port: 8087 },
  { id: 'workflow', name: 'Workflow', gatewayPrefix: '/api/workflows', apiPath: '/api/v1/workflows', bootstrapPath: '/api/v1/bootstrap/health', port: 8088 },
  { id: 'notification', name: 'Notification', gatewayPrefix: '/api/notifications', apiPath: '/api/v1/notifications', bootstrapPath: '/api/v1/bootstrap/health', port: 8089 },
  { id: 'search', name: 'Search', gatewayPrefix: '/api/search', apiPath: '/api/v1/search', bootstrapPath: '/api/v1/bootstrap/health', port: 8090 },
  { id: 'ai', name: 'AI', gatewayPrefix: '/api/ai', apiPath: '/api/v1/ai', bootstrapPath: '/api/v1/bootstrap/health', port: 8091 },
  { id: 'audit', name: 'Audit', gatewayPrefix: '/api/audits', apiPath: '/api/v1/audits', bootstrapPath: '/api/v1/bootstrap/health', port: 8092 },
  { id: 'subscription', name: 'Subscription', gatewayPrefix: '/api/subscriptions', apiPath: '/api/v1/subscriptions', bootstrapPath: '/api/v1/bootstrap/health', port: 8093 },
  { id: 'report', name: 'Report', gatewayPrefix: '/api/reports', apiPath: '/api/v1/reports', bootstrapPath: '/api/v1/bootstrap/health', port: 8094 },
  { id: 'admin', name: 'Admin', gatewayPrefix: '/api/admin', apiPath: '/api/v1/admin', bootstrapPath: '/api/v1/bootstrap/health', port: 8095 },
];

export function gatewayUrl(service: ServiceDefinition, path: string): string {
  if (!service.gatewayPrefix) {
    return path;
  }
  return `${service.gatewayPrefix}${path}`;
}

export function newIdempotencyKey(): string {
  return crypto.randomUUID();
}

export function newGuid(): string {
  return crypto.randomUUID();
}
