import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../composables/useAuth'

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 主界面已经在 App.vue 中实现，这里可以添加其他页面的路由
  ]
})

// 路由守卫
// @ts-ignore - 忽略类型错误
router.beforeEach((to, from, next) => {
  const { isAuthenticated } = useAuth()
  
  // 如果需要在某些路由添加认证检查，可以在这里实现
  next()
})

export default router
