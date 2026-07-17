import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const gatewayUrl = env.VITE_GATEWAY_URL || 'http://localhost:9080';
  const identityUrl = env.VITE_IDENTITY_URL || 'http://localhost:8081';

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        // Prefer direct identity so KaratKart auth works without the gateway.
        '/api/identity': {
          target: identityUrl,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api\/identity/, ''),
        },
        '/api': {
          target: gatewayUrl,
          changeOrigin: true,
        },
        '/actuator': {
          target: gatewayUrl,
          changeOrigin: true,
        },
      },
    },
  };
});
