import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Si el puerto está ocupado usa el siguiente disponible automáticamente
    strictPort: false,
    proxy: {
      '/api': {
        target: 'http://localhost:3005',
        changeOrigin: true,
        secure: false,
        // 20 segundos — Spring Boot puede tardar en responder al arrancar
        timeout: 20000,
        proxyTimeout: 20000,
        configure: (proxy) => {
          proxy.on('error', (err) => {
            // Solo mostrar en consola, no crashear el dev server
            console.warn('[Proxy /api] No se pudo conectar al BFF en :3005 —', err.message)
          })
          proxy.on('proxyReq', (_proxyReq, req) => {
            if (req.url?.includes('/auth/') || req.url?.includes('/mascotas/')) {
              console.log('[Proxy]', req.method, req.url)
            }
          })
        }
      }
    }
  },
  build: {
    chunkSizeWarningLimit: 600
  }
})
