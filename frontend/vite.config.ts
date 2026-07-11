import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const gatewayUrl = env.VITE_GATEWAY_URL || 'http://localhost:9080';

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
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
