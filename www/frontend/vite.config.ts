import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  },
  build: {
    // 优化构建性能
    target: 'esnext',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
    // 代码分割
    rollupOptions: {
      output: {
        manualChunks: {
          // 将 Vue 相关库分离到单独的 chunk
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // 将工具库分离
          'utils': ['axios', 'marked', 'dompurify'],
          // 将大型组件分离
          'admin': [
            './src/components/admin/AdminLayout.vue',
            './src/components/admin/views/SystemSettings.vue',
            './src/components/admin/views/UserList.vue',
            './src/components/admin/views/DraftReviewDashboard.vue'
          ]
        }
      }
    },
    // 优化 chunk 大小
    chunkSizeWarningLimit: 1000,
  },
  // 开发服务器优化
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', 'axios', 'marked', 'dompurify'],
  },
})
